/*? if neoforge {*/
/*package com.calculatorsteam.dynamicpack.platform.neoforge

import com.calculatorsteam.dynamicpack.platform.PlatformEntrypoint
import net.minecraft.client.player.LocalPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent

@EventBusSubscriber
object NeoForgeEvent {

    @SubscribeEvent
    fun onJoin(event: EntityJoinLevelEvent) {
        (event.entity as? LocalPlayer)?.let { player ->
            PlatformEntrypoint.instance.onWorldJoin(player)
        }
    }
}
*//*?}*/