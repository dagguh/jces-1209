package jces1209

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions.EU_WEST_1
import com.amazonaws.regions.Regions.US_EAST_1
import com.atlassian.performance.tools.aws.api.Aws
import com.atlassian.performance.tools.aws.api.DependentResources
import com.atlassian.performance.tools.aws.api.Investment
import com.atlassian.performance.tools.aws.api.SshKeyFormula
import com.atlassian.performance.tools.awsinfrastructure.api.network.NetworkFormula
import com.atlassian.performance.tools.awsinfrastructure.api.virtualusers.MulticastVirtualUsersFormula
import com.atlassian.performance.tools.awsinfrastructure.api.virtualusers.ProvisionedVirtualUsers
import com.atlassian.performance.tools.infrastructure.api.virtualusers.DirectResultsTransport
import com.atlassian.performance.tools.io.api.dereference
import com.atlassian.performance.tools.io.api.ensureDirectory
import java.nio.file.Path
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture

class AwsVus : VirtualUsersSource {

    override fun obtainVus(
        resultsTarget: Path,
        workspace: Path
    ): ProvisionedVirtualUsers<*> {
        val aws = prepareAws()
        val nonce = UUID.randomUUID().toString()
        val sshKey = SshKeyFormula(
            ec2 = aws.ec2,
            workingDirectory = workspace,
            prefix = nonce,
            lifespan = Duration.ofMinutes(30)
        ).provision()
        val investment = Investment(
            useCase = "Compare two Jiras the Falcon way",
            lifespan = Duration.ofMinutes(30)
        )
        val network = NetworkFormula(
            investment = investment,
            aws = aws
        ).provision()
        val provisioned = MulticastVirtualUsersFormula.Builder(
            nodes = 6,
            shadowJar = dereference("jpt.virtual-users.shadow-jar")
        )
            .browser(Chromium77())
            .network(network)
            .build()
            .provision(
                investment = investment,
                shadowJarTransport = aws.virtualUsersStorage(nonce),
                resultsTransport = workAroundDirectResultTransportRaceCondition(resultsTarget),
                roleProfile = aws.shortTermStorageAccess(),
                key = CompletableFuture.completedFuture(sshKey),
                aws = aws
            )
        return ProvisionedVirtualUsers(
            virtualUsers = provisioned.virtualUsers,
            resource = DependentResources(
                user = provisioned.resource,
                dependency = sshKey.remote
            )
        )
    }

    private fun workAroundDirectResultTransportRaceCondition(
        resultsTarget: Path
    ): DirectResultsTransport {
        resultsTarget.resolve("virtual-users").ensureDirectory()
        return DirectResultsTransport(resultsTarget)
    }

    private fun prepareAws() = Aws.Builder(US_EAST_1)
        .credentialsProvider(
            AWSCredentialsProviderChain(
                STSAssumeRoleSessionCredentialsProvider.Builder(
                    "arn:aws:iam::695067801333:role/server-gdn-bamboo",
                    UUID.randomUUID().toString()
                ).build(),
                ProfileCredentialsProvider("jpt-dev"),
                EC2ContainerCredentialsProviderWrapper(),
                DefaultAWSCredentialsProviderChain()
            )
        )
        .regionsWithHousekeeping(listOf(EU_WEST_1, US_EAST_1)) // https://server-gdn-bamboo.internal.atlassian.com/browse/JIRA-JPTH
        .batchingCloudformationRefreshPeriod(Duration.ofSeconds(20))
        .build()
}
