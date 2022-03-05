from time import sleep as wait
from selenium.webdriver.common.by import By
from qaV2.QA.controller.abstract_controller import AbstractController


class Queue(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.song_title = (
            By.ID, "com.example.wavegroove:id/songTitleSongRowTextView")
        self.actual_song_title = (
            By.ID, "com.example.wavegroove:id/queueTitleTextView")
        self.fast_forward = (
            By.ID, "com.example.wavegroove:id/fastForwardMusicBarImageButton")
        self.empty_queue_test = (
            By.ID, "com.example.wavegroove:id/emptyQueueTextView")
        self.dots_song = (
            By.ID, "com.example.wavegroove:id/verticalDotsSongRowImageView")
        self.move_song_top = (
            By.ID,
            "com.example.wavegroove:id/moveTopModalWindowQueuedSongButton")
        self.move_song_bottom = (
            By.ID,
            "com.example.wavegroove:id/moveBottonModalWindowQueuedSongButton")
        self.remove_song = (
            By.ID,
            "com.example.wavegroove:id/removeFromQueueCoverModalWindowQueuedSongButton")

    def get_actual_song_title(self):
        return self.get_text(self.actual_song_title)

    def get_songs_titles(self):
        wait(2)
        titles = self.driver.find_elements(*self.song_title)
        return [t.text for t in titles]

    def click_fast_forward(self):
        return self.click_button(self.fast_forward)

    def get_empty_queue_text(self):
        return self.get_text(self.empty_queue_test)

    def click_song_options(self, idx):
        return self.click_button(self.dots_song, idx=idx, delay=2)

    def click_move_song_top(self):
        return self.click_button(self.move_song_top)

    def click_move_song_bottom(self):
        return self.click_button(self.move_song_bottom)

    def click_remove_song(self):
        return self.click_button(self.remove_song)
