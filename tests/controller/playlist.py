from time import sleep as wait
from selenium.webdriver.common.by import By
from qaV2.QA.controller.abstract_controller import AbstractController


class Playlist(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.song_title = (
            By.ID, "com.example.wavegroove:id/songTitleSongRowTextView")
        self.start_playlist = (
            By.ID, "com.example.wavegroove:id/innerTextInCustomButtonTextView")

    def get_songs_titles(self):
        wait(2)
        titles = self.driver.find_elements(*self.song_title)
        return [t.text for t in titles]

    def click_start_playlist(self):
        return self.click_button(self.start_playlist, idx=1, delay=2)

    def click_add_files(self):
        return self.click_button(self.start_playlist, idx=0, delay=2)
