package jces1209

import com.atlassian.performance.tools.aws.api.UnallocatedResource
import com.atlassian.performance.tools.awsinfrastructure.api.virtualusers.ProvisionedVirtualUsers
import com.atlassian.performance.tools.infrastructure.api.virtualusers.DirectResultsTransport
import com.atlassian.performance.tools.infrastructure.api.virtualusers.MulticastVirtualUsers
import com.atlassian.performance.tools.io.api.ensureDirectory
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshHost
import extract.to.lib.jpt.report.PreparedUnixVirtualUser
import java.nio.file.Path
import java.nio.file.Paths

class CustomVus : VirtualUsersSource {

    override fun obtainVus(
        resultsTarget: Path,
        workspace: Path
    ): ProvisionedVirtualUsers<*> {
        val nodeIps = listOf(
            "1.2.3.4",
            "5.6.7.8"
        )
        val sshHosts = nodeIps.map { nodeIp ->
            SshHost(nodeIp, "ec2-user", Paths.get("/path/to/ssh/id_rsa"))
        }
        val nodes = sshHosts.map { sshHost ->
            PreparedUnixVirtualUser(
                ssh = Ssh(sshHost),
                jarName = "custom-vu.jar", // remote path to the JAR from the local custom-vu/build/libs/custom-vu.jar
                resultsTransport = transportResultsViaSsh(resultsTarget)
            )
        }
        val virtualUsers = MulticastVirtualUsers(nodes)
        return ProvisionedVirtualUsers(
            virtualUsers = virtualUsers,
            resource = UnallocatedResource()
        )
    }

    private fun transportResultsViaSsh(
        resultsTarget: Path
    ): DirectResultsTransport {
        resultsTarget.resolve("virtual-users").ensureDirectory()
        return DirectResultsTransport(resultsTarget)
    }
}
