from time import sleep as wait
from selenium.webdriver.common.by import By
from qaV2.QA.controller.abstract_controller import AbstractController


class Home(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.accept_storage_button = (
            By.ID, "com.android.packageinstaller:id/permission_allow_button")
        self.notification_text = (By.ID,
                                  "com.example.wavegroove:id/customNotificationnothingIsPlayedTextView")
        self.accept_new_playlist_button = (
            By.ID, "com.example.wavegroove:id/acceptNewPlaylistButton")
        self.cancel_new_playlist_button = (
            By.ID, "com.example.wavegroove:id/cancelNewPlaylistButton")
        self.playlist_name = (
            By.ID, "com.example.wavegroove:id/playlistNameTextView")
        self.new_playlist_name_edit_text_field = (
            By.ID, "com.example.wavegroove:id/newPlaylistNameEditText")
        self.change_album_cover_button = (By.ID,
                                          "com.example.wavegroove:id/changePlaylistCoverModalWindowPlaylistButton")
        self.delete_playlist_button = (By.ID,
                                       "com.example.wavegroove:id/deletePlaylistModalWindowPlaylistButton")
        self.playlist_cover = (
            By.ID, "com.example.wavegroove:id/singlePlaylistImageView")
        self.add_files_to_playlist_button = (By.ID,
                                             "com.example.wavegroove:id/addFilesToPlaylistModalWindowPlaylistButton")
        self.queue_button = (
        By.ID, "com.example.wavegroove:id/playlistNuteImageView")
        self.clear_queue = (By.ID, "com.example.wavegroove:id/clearQueueButton")
        self.main_button = (
        By.ID, "com.example.wavegroove:id/hifiMainButtonImageView")
        self.fast_forward = (
        By.ID, "com.example.wavegroove:id/fastForwardMusicBarImageButton")

    def accept_access_to_storage(self):
        status = self.click_button(self.accept_storage_button, delay=2)
        return status

    def cancel_new_playlist_modal(self):
        status = self.click_button(self.cancel_new_playlist_button, delay=2)
        return status

    def accept_new_playlist_modal(self):
        status = self.click_button(self.accept_new_playlist_button, delay=2)
        return status

    def is_new_playlist_button_visible(self):
        return self.is_visible(self.accept_new_playlist_button)

    def get_all_playlists_names(self):
        wait(2)
        playlists = self.driver.find_elements(*self.playlist_name)
        playlists_names = [p.text for p in playlists]
        return playlists_names

    def update_text_new_playlist_name_edit_text_field(self, text):
        wait(2)
        edit_text = self.driver.find_elements(
            *self.new_playlist_name_edit_text_field)
        if edit_text:
            edit_text[0].clear()
            edit_text[0].send_keys(text)
            return True
        return False

    def long_click_on_playlist(self, playlist_name):
        status = False
        playlists = self.driver.find_elements(*self.playlist_name)
        for playlist in playlists:
            if playlist.text == playlist_name:
                status = self.long_click(playlist)
                break
        return status

    def click_change_album_cover(self):
        return self.click_button(self.change_album_cover_button)

    def get_playlists_image_views(self):
        return self.driver.find_elements(*self.playlist_cover)

    def click_delete_playlist(self):
        return self.click_button(self.delete_playlist_button)

    def click_add_files_to_playlist(self):
        return self.click_button(self.add_files_to_playlist_button)

    def go_to_queue(self):
        return self.click_button(self.queue_button)

    def click_clear_queue(self):
        return self.click_button(self.clear_queue)

    def click_main_button(self):
        return self.click_button(self.main_button)

    def click_fast_forward(self):
        return self.click_button(self.fast_forward)
