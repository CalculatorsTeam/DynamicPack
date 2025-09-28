/*? if forge {*/
/*package com.calculatorsteam.dynamicpack.platform.forge

import com.calculatorsteam.dynamicpack.platform.PlatformEntrypoint
import net.minecraft.client.player.LocalPlayer
import net.minecraftforge.event.entity.EntityJoinLevelEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber
object ForgeEvent {

    @SubscribeEvent
    fun onJoin(event: EntityJoinLevelEvent) {
        (event.entity as? LocalPlayer)?.let { player ->
            PlatformEntrypoint.instance.onWorldJoin(player)
        }
    }
}
*//*?}*/