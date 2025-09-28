/*? if fabric {*/
package com.calculatorsteam.dynamicpack.platform.fabric

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.client.config.ConfigScreenBuilder
import com.calculatorsteam.dynamicpack.client.config.NoYACLScreen
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { parent ->
            if (DynamicPackMod.instance.isModExists("yet_another_config_lib_v3")) {
                ConfigScreenBuilder.create(parent)
            } else {
                NoYACLScreen(parent)
            }
        }
    }
}
/*?}*/