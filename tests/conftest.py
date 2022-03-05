import pytest
from qaV2.QA.controller.tools_controller import ToolsAppium


@pytest.fixture()
def Tools():
    appium = ToolsAppium()
    yield appium
    appium.shut_down()
