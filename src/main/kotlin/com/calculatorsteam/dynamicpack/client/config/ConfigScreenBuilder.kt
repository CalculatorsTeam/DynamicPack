package com.calculatorsteam.dynamicpack.client.config

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.sync.SyncingTask
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.slider.IntegerSliderController
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object ConfigScreenBuilder {
    @JvmStatic
    fun create(parent: Screen): Screen {
        val config = YetAnotherConfigLib.createBuilder()
            .title(Component.literal("DynamicPack"))
            .category(buildGeneralCategory())
            .category(buildNetworkCategory())
            .category(buildDebugCategory())
            .save { DynamicPackMod.config.save() }
            .build()

        return YACLScreen(config, parent)
    }

    private fun buildGeneralCategory(): ConfigCategory =
        ConfigCategory.createBuilder()
            .name(Component.translatable("dynamicpack.screen.config.category.general"))
            .group(
                OptionGroup.createBuilder()
                    .option(
                        Option.createBuilder<Boolean>()
                            .name(Component.translatable("dynamicpack.screen.config.category.general.isAutoUpdateAtLaunch"))
                            .binding(
                                Config.DEF.isAutoUpdateAtLaunch(),
                                { DynamicPackMod.config.isAutoUpdateAtLaunch() },
                                { DynamicPackMod.config.setAutoUpdateAtLaunch(it) }
                            )
                            .controller { BooleanControllerBuilder.create(it).yesNoFormatter() }
                            .build()
                    )
                    .option(
                        Option.createBuilder<Boolean>()
                            .name(Component.translatable("dynamicpack.screen.config.category.general.updateOnlyEnabledPacks"))
                            .binding(
                                Config.DEF.isUpdateOnlyEnabledPacks(),
                                { DynamicPackMod.config.isUpdateOnlyEnabledPacks() },
                                { DynamicPackMod.config.setUpdateOnlyEnabledPacks(it) }
                            )
                            .controller { BooleanControllerBuilder.create(it).yesNoFormatter() }
                            .build()
                    )
                    .build()
            )
            .build()

    private fun buildDebugCategory(): ConfigCategory =
        ConfigCategory.createBuilder()
            .name(Component.translatable("dynamicpack.screen.config.category.debug"))
            .tooltip(Component.translatable("dynamicpack.screen.config.category.debug.description").withStyle(ChatFormatting.RED))
            .group(
                OptionGroup.createBuilder()
                    .name(Component.literal("Calculator's category ^_^"))
                    .option(
                        Option.createBuilder<Boolean>()
                            .name(Component.translatable("dynamicpack.screen.config.category.debug.logAllFilesChanges"))
                            .binding(
                                Config.DEF.isLogAllFilesChanges(),
                                { DynamicPackMod.config.isLogAllFilesChanges() },
                                { DynamicPackMod.config.setLogAllFilesChanges(it) }
                            )
                            .controller(TickBoxControllerBuilder::create)
                            .build()
                    )
                    .option(
                        Option.createBuilder<Boolean>()
                            .name(Component.translatable("dynamicpack.screen.config.category.debug.ignoreHidden"))
                            .binding(
                                Config.DEF.dynamicRepoIsIgnoreHiddenContentFlag(),
                                { DynamicPackMod.config.dynamicRepoIsIgnoreHiddenContentFlag() },
                                { DynamicPackMod.config.setDebugIgnoreHiddenFlagInContents(it) }
                            )
                            .controller(TickBoxControllerBuilder::create)
                            .build()
                    )
                    .option(
                        Option.createBuilder<Boolean>()
                            .name(Component.literal("INTERRUPT UPDATING"))
                            .description(OptionDescription.of(Component.literal("EXPERIMENTAL FUNCTIONAL")))
                            .binding(object : Binding<Boolean> {
                                override fun setValue(value: Boolean) {
                                    SyncingTask.currentRootSyncBuilder?.interrupt()
                                }
                                override fun getValue(): Boolean = false
                                override fun defaultValue(): Boolean = false
                            })
                            .controller(TickBoxControllerBuilder::create)
                            .build()
                    )
                    .build()
            )
            .build()

    private fun buildNetworkCategory(): ConfigCategory =
        ConfigCategory.createBuilder()
            .name(Component.translatable("dynamicpack.screen.config.category.network"))
            .tooltip(Component.translatable("dynamicpack.screen.config.category.network.tooltip"))
            .group(
                OptionGroup.createBuilder()
                    .name(Component.translatable("dynamicpack.screen.config.category.network.group.name"))
                    .description(OptionDescription.of(Component.translatable("dynamicpack.screen.config.category.network.group.description")))
                    .option(
                        Option.createBuilder<Int>()
                            .name(Component.translatable("dynamicpack.screen.config.category.network.bufferSize.name"))
                            .binding(
                                Config.DEF.getNetworkBufferSize(),
                                { DynamicPackMod.config.getNetworkBufferSize() },
                                { DynamicPackMod.config.setNetworkBufferSize(it) }
                            )
                            .controller {
                                IntegerSliderControllerBuilder.create(it)
                                    .step(256)
                                    .range(256, 8192)
                            }
                            .build()
                    )
                    .option(
                        Option.createBuilder<Int>()
                            .name(Component.translatable("dynamicpack.screen.config.category.network.multithread.threads.name"))
                            .binding(
                                Config.DEF.getNetworkMultithreadDownloadThreads(),
                                { DynamicPackMod.config.getNetworkMultithreadDownloadThreads() },
                                { DynamicPackMod.config.setNetworkMultithreadDownloadThreads(it) }
                            )
                            .controller { integerOption ->
                                IntegerSliderControllerBuilder.create(integerOption)
                                    .step(1)
                                    .formatValue { value ->
                                        when {
                                            value >= 255 -> Component.literal("^_^ ")
                                                .withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.GOLD)
                                                .append(Component.literal(value.toString()).withStyle(ChatFormatting.RESET, ChatFormatting.BOLD, ChatFormatting.GOLD))
                                            value > 80 -> Component.literal("X_X ")
                                                .withStyle(ChatFormatting.DARK_RED)
                                                .append(Component.literal(value.toString()).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED))
                                            value > 64 -> Component.literal("X_0 ")
                                                .withStyle(ChatFormatting.DARK_RED)
                                                .append(Component.literal(value.toString()).withStyle(ChatFormatting.BOLD))
                                            value > 32 -> Component.literal("0_0 ")
                                                .withStyle(ChatFormatting.RED)
                                                .append(Component.literal(value.toString()).withStyle(ChatFormatting.BOLD))
                                            value > 16 -> Component.literal("OwO ")
                                                .withStyle(ChatFormatting.YELLOW)
                                                .append(Component.literal(value.toString()).withStyle(ChatFormatting.BOLD))
                                            else -> IntegerSliderController.DEFAULT_FORMATTER.apply(value)
                                        }
                                    }
                                    .range(1, 255)
                            }
                            .build()
                    )
                    .build()
            )
            .build()
}