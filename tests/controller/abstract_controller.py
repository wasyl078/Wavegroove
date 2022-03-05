import subprocess as sp
from time import sleep
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction
from selenium.webdriver.common.by import By


class AbstractController(object):

    def __init__(self, driver):
        self.driver = driver

    def get_text(self, locator, delay=2):
        sleep(delay)
        text = None
        elems = self.driver.find_elements(*locator)
        if elems:
            text = elems[0].text
        return text

    def click_button(self, locator, idx=0, delay=2):
        sleep(delay)
        status = False
        buttons = self.driver.find_elements(*locator)
        if buttons:
            buttons[idx].click()
            status = True
        return status

    def long_click(self, element, delay=2):
        sleep(delay)
        status = False
        if element:
            actions = TouchAction(self.driver)
            actions.long_press(element)
            actions.perform()
            status = True
        return status

    def is_visible(self, locator, delay=2):
        sleep(delay)
        status = False
        elems = self.driver.find_elements(*locator)
        if elems:
            status = elems[0].is_displayed()
        return status
