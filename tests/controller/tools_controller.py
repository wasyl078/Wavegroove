import subprocess as sp
from time import sleep as wait

from appium import webdriver
from selenium.webdriver.common.by import By
from qaV2.QA.controller.abstract_controller import AbstractController
from qaV2.QA.controller.home import Home
from qaV2.QA.controller.imported import Imported
from qaV2.QA.controller.playlist import Playlist
from qaV2.QA.controller.queue import Queue
from qaV2.QA.controller.song_view import SongView


class ToolsAppium(AbstractController):
    def __init__(self):
        # Set up
        self.init_server()
        super().__init__(self.driver)

        self.home = Home(self.driver)
        self.imported = Imported(self.driver)
        self.playlist = Playlist(self.driver)
        self.queue = Queue(self.driver)
        self.song_view = SongView(self.driver)

        # Locators
        self.notification_text = (By.ID,
                                  "com.example.wavegroove:id/customNotificationnothingIsPlayedTextView")
        self.options_three_dots = (
            By.ID, "com.example.wavegroove:id/allPlaylistsHamburgerImageButton")
        self.options_create_new_playlsit = (
            By.ID, "com.example.wavegroove:id/createNewPlaylistButton")
        self.documents_button = (By.ID, "android:id/icon")
        self.picture = (By.ID, "com.android.documentsui:id/icon_thumb")

    # Utils - Set Up
    def init_server(self):
        sp.Popen("appium", shell=True, stdout=sp.DEVNULL, stderr=sp.DEVNULL)
        wait(5)
        caps = {
            "platformName": "Android", "platformVersion": "",
            "deviceName": "emulator-5554",
            "appPackage": "com.example.wavegroove",
            "appActivity": ".general.MainActivity"
        }
        self.driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", caps)

    # Utils - Shut Down
    def shut_down(self):
        self.driver.quit()

    # Utils notification
    def get_notification_text(self):
        return self.get_text(self.notification_text)

    def scroll_top_bar_down(self):
        wait(2)
        self.driver.swipe(20, 20, 20, 500, 400)
        return True

    def scroll_top_bar_up(self):
        wait(2)
        self.driver.swipe(20, 300, 20, 20, 400)
        return True

    def slide_down(self, repeats=1):
        for _ in range(repeats):
            wait(2)
            self.driver.swipe(300, 1200, 300, 200, 500)
        wait(2)
        return True

    # Utils general
    def click_back_button(self):
        wait(2)
        self.driver.press_keycode(4)

    def click_home_button(self):
        wait(2)
        self.driver.press_keycode(3)

    def click_recent_apps_button(self):
        wait(2)
        self.driver.press_keycode(187)

    def scroll_down(self):
        wait(2)
        self.driver.swipe(470, 1400, 470, 300, 400)
        return True

    def click_options_three_dots(self):
        return self.click_button(self.options_three_dots)

    def click_options_create_new_playlist(self):
        return self.click_button(self.options_create_new_playlsit)

    def click_documents(self):
        return self.click_button(self.documents_button)

    def click_last_picture(self):
        status = False
        pictures = self.driver.find_elements(*self.picture)
        if pictures:
            pictures[-1].click()
            status = True
            wait(3)
        return status
