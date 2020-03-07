package leaderboard.config

import software.amazon.awssdk.services.dynamodb.model.BillingMode

final case class ProvisioningCfg(read: Long, write: Long, mode: BillingMode)
