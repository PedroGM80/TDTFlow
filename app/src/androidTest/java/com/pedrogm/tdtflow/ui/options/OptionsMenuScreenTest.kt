package com.pedrogm.tdtflow.ui.options

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.pedrogm.tdtflow.ui.options.components.OptionsMenuContent
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OptionsMenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        state: OptionsMenuState = OptionsMenuState(isOpen = true),
        onIntent: (OptionsMenuIntent) -> Unit = {},
        showBrokenChannels: Boolean = false,
        onToggleBroken: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TDTFlowTheme {
                OptionsMenuContent(
                    state = state,
                    onIntent = onIntent,
                    showBrokenChannels = showBrokenChannels,
                    onToggleBroken = onToggleBroken
                )
            }
        }
    }

    @Test
    fun bottomSheet_isDisplayed_whenOpen() {
        setContent()

        composeTestRule
            .onNodeWithTag("options_bottom_sheet")
            .assertIsDisplayed()
    }

    @Test
    fun themeButtons_areDisplayed() {
        setContent()

        AppTheme.entries.forEach { theme ->
            composeTestRule
                .onNodeWithTag("theme_button_${theme.name}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun tappingThemeButton_firesSelectThemeIntent() {
        val intents = mutableListOf<OptionsMenuIntent>()
        setContent(onIntent = { intents.add(it) })

        composeTestRule
            .onNodeWithTag("theme_button_DARK")
            .performClick()

        assertEquals(
            OptionsMenuIntent.SelectTheme(AppTheme.DARK),
            intents.last()
        )
    }

    @Test
    fun tappingThemeButtonLight_firesSelectThemeIntent() {
        val intents = mutableListOf<OptionsMenuIntent>()
        setContent(onIntent = { intents.add(it) })

        composeTestRule
            .onNodeWithTag("theme_button_LIGHT")
            .performClick()

        assertEquals(
            OptionsMenuIntent.SelectTheme(AppTheme.LIGHT),
            intents.last()
        )
    }

    @Test
    fun brokenChannelsSwitch_isDisplayed() {
        setContent()

        composeTestRule
            .onNodeWithTag("broken_channels_switch")
            .assertIsDisplayed()
    }

    @Test
    fun brokenChannelsSwitch_isOff_byDefault() {
        setContent(showBrokenChannels = false)

        composeTestRule
            .onNodeWithTag("broken_channels_switch")
            .assertIsOff()
    }

    @Test
    fun brokenChannelsSwitch_isOn_whenEnabled() {
        setContent(showBrokenChannels = true)

        composeTestRule
            .onNodeWithTag("broken_channels_switch")
            .assertIsOn()
    }

    @Test
    fun tappingBrokenChannelsSwitch_invokesToggleCallback() {
        var toggled = false
        setContent(onToggleBroken = { toggled = true })

        composeTestRule
            .onNodeWithTag("broken_channels_switch")
            .performClick()

        assertTrue(toggled)
    }

    @Test
    fun languageOptions_areDisplayed() {
        setContent()

        AppLanguage.entries.forEach { language ->
            composeTestRule
                .onNodeWithTag("language_option_${language.name}")
                .assertIsDisplayed()
        }
    }

    @Test
    fun tappingLanguageOption_firesSelectLanguageIntent() {
        val intents = mutableListOf<OptionsMenuIntent>()
        setContent(onIntent = { intents.add(it) })

        composeTestRule
            .onNodeWithTag("language_option_ES")
            .performClick()

        assertEquals(
            OptionsMenuIntent.SelectLanguage(AppLanguage.ES),
            intents.last()
        )
    }

    @Test
    fun tappingLanguageOptionEN_firesSelectLanguageIntent() {
        val intents = mutableListOf<OptionsMenuIntent>()
        setContent(onIntent = { intents.add(it) })

        composeTestRule
            .onNodeWithTag("language_option_EN")
            .performClick()

        assertEquals(
            OptionsMenuIntent.SelectLanguage(AppLanguage.EN),
            intents.last()
        )
    }
}
