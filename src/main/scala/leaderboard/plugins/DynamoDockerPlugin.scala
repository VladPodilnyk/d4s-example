package leaderboard.plugins

import d4s.test.envs.D4SDockerModule
import izumi.distage.docker.modules.DockerSupportModule
import izumi.distage.model.definition.StandardAxis.Scene
import izumi.distage.plugins.PluginDef
import zio.{IO, Task}

object DynamoDockerPlugin extends PluginDef {
  tag(Scene.Managed)
  // add docker support dependencies
  include(DockerSupportModule[Task])
  // d4s docker module
  include(D4SDockerModule[IO])
}
