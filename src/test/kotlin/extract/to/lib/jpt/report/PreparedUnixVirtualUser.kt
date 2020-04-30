package extract.to.lib.jpt.report

import com.atlassian.performance.tools.infrastructure.api.virtualusers.ResultsTransport
import com.atlassian.performance.tools.infrastructure.api.virtualusers.VirtualUsers
import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions

/**
 * Launches VUs via SSH without installing additional software. Therefore doesn't assume package managers like apt-get.
 * Assumes that Java is already installed and the [jarName] is already uploaded.
 *
 * @param [ssh] connects to the node
 * @param [jarName] points to a VU JAR on the node
 * @param [resultsTransport] transports the results from the node
 */
class PreparedUnixVirtualUser(
    private val ssh: Ssh,
    private val jarName: String,
    private val resultsTransport: ResultsTransport
) : VirtualUsers {

    override fun applyLoad(options: VirtualUserOptions) {
        val launchCommand = launchVuCommand(options)
        val timeout = options.behavior.load.total + options.behavior.maxOverhead
        ssh.newConnection().execute(launchCommand, timeout)
    }

    private fun launchVuCommand(options: VirtualUserOptions): String {
        val javaParams = mutableListOf(
            "-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n",
            "-jar $jarName"
        )
        val cliArgs = options.toCliArgs().map { "'$it'" }
        val redirects = listOf(
            "2>virtual-users-error.log",
            "> virtual-users-out.log"
        )
        val javaArgs = (javaParams + cliArgs + redirects).joinToString(" ")
        return "java $javaArgs"
    }

    override fun gatherResults() {
        val uploadDirectory = "results"
        val resultsDirectory = "$uploadDirectory/virtual-users/vu-node-${ssh.host.ipAddress}"
        ssh.newConnection().use { shell ->
            listOf(
                "mkdir -p $resultsDirectory",
                "mv test-results $resultsDirectory",
                "mv diagnoses $resultsDirectory",
                "mv virtual-users.log $resultsDirectory",
                "mv virtual-users-out.log $resultsDirectory",
                "mv virtual-users-error.log $resultsDirectory",
                "cp /var/log/syslog $resultsDirectory",
                "cp /var/log/cloud-init.log $resultsDirectory",
                "cp /var/log/cloud-init-output.log $resultsDirectory",
                "find $resultsDirectory -empty -type f -delete"
            ).forEach { shell.safeExecute(it) }
            resultsTransport.transportResults(uploadDirectory, shell)
        }
    }
}
