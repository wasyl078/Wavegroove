from time import sleep as wait

from appium.webdriver.common.touch_action import TouchAction
from selenium.webdriver.common.by import By
from qaV2.QA.controller.abstract_controller import AbstractController


class Imported(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.import_from_memory_button = (
            By.ID, "com.example.wavegroove:id/innerTextInCustomButtonTextView")
        self.just_once_button = (By.ID, "android:id/button_once")
        self.mp3_file_icon = (By.ID, "com.android.documentsui:id/icon_thumb")
        self.open_file_button = (By.ID, "com.android.documentsui:id/menu_sort")
        self.song_title = (
            By.ID, "com.example.wavegroove:id/songTitleImportedSongRowTextView")
        self.check_box = (
            By.ID, "com.example.wavegroove:id/toAddImportedSongRowCheckBox")

        self.add_selected_to_playlist_button = (
            By.ID, "com.example.wavegroove:id/innerTextInCustomButtonTextView")
        self.song_image = (
            By.ID, "com.example.wavegroove:id/importedSongRowImageView")
        self.delete_song = (
            By.ID, "com.example.wavegroove:id/deleteSongModalWindowImportedSongButton")

    def click_import_from_memory(self):
        return self.click_button(self.import_from_memory_button)

    def click_just_once(self):
        return self.click_button(self.just_once_button)

    def click_one_mp3_files(self, idxs):
        wait(2)
        pictures = self.driver.find_elements(*self.mp3_file_icon)
        if pictures:
            for i, elem in enumerate(pictures):
                if i in idxs:
                    self.long_click(elem)

    def click_load_file(self):
        return self.click_button(self.open_file_button)

    def get_imported_songs_titles(self):
        wait(2)
        titles = self.driver.find_elements(*self.song_title)
        return [t.text for t in titles]

    def click_check_boxes(self, idxs):
        print(idxs)
        wait(2)
        check_boxes = self.driver.find_elements(*self.check_box)
        if check_boxes:
            for i, elem in enumerate(check_boxes):
                print(i)
                if i in idxs:
                    actions = TouchAction(self.driver)
                    actions.tap(elem)
                    actions.perform()
                    wait(2)

    def click_add_selected_to_playlist(self):
        return self.click_button(self.add_selected_to_playlist_button, idx=1,
                                 delay=2)

    def long_click_song(self, idx):
        wait(2)
        songs = self.driver.find_elements(*self.song_image)
        print(songs)
        if songs:
            return self.long_click(songs[idx])
        return False

    def click_delete_song(self):
        return self.click_button(self.delete_song)