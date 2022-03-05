from time import sleep as wait
from selenium.webdriver.common.by import By
from qaV2.QA.controller.abstract_controller import AbstractController


class SongView(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.actual_song_title = (
            By.ID, "com.example.wavegroove:id/songTitleTextView")
        self.fast_forward = (
            By.ID, "com.example.wavegroove:id/fastForwardImageButton")
        self.repeat_button = (
            By.ID, "com.example.wavegroove:id/repeatImageButton")

    def get_actual_song_title(self):
        return self.get_text(self.actual_song_title)

    def click_fast_forward(self):
        return self.click_button(self.fast_forward)

    def click_repeat_button(self):
        return self.click_button(self.repeat_button)
