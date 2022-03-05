import pytest
import os
from time import sleep as wait


def test_app_general_running(Tools):
    """
    This test verifies whether app is properly running.

    *** Test Procedure ***

    * Open app
    * Accept access to storage
    * Check notification in top bar
    * Hide app
    * Wait few seconds and go back to app
    * Check if app is still online

    *** Pass/Fail Criteria ***

    * Notifcation says: "Nothing is played right now."
    * App does not crash after hiding
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Check notification
    assert Tools.scroll_top_bar_down()
    notification_text = Tools.get_notification_text()
    assert notification_text == "Nothing is played right now."
    assert Tools.scroll_top_bar_up()

    # Check after hiding
    Tools.click_home_button()
    wait(5)
    Tools.click_recent_apps_button()
    Tools.click_recent_apps_button()
    assert Tools.home.is_new_playlist_button_visible()


def test_app_switch_off(Tools):
    """
    This test verifies whether app can be properly switched off.

    *** Test Procedure ***

    * Open app
    * Accept access to storage
    * Cancel modal window
    * Click return button
    * Wait few seconds
    * Check absence of app notification

    *** Pass/Fail Criteria ***

    * App should be switched off after clicking "back" in home view
    * When app is switched off, there is no notificcation
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Switch off app and wait
    Tools.home.cancel_new_playlist_modal()
    Tools.click_back_button()
    wait(3)

    # Check notifications
    assert Tools.scroll_top_bar_down()
    assert Tools.get_notification_text() is None, \
        "App was not switched off properly."


def test_new_playlists(Tools):
    """
    This test verifies whether app can properly handle creating new playlists,
    and whether there cannot be two playlists with same name.

    *** Test Procedure ***

    * Open app
    * Accept access to storage
    * Accept modal window
    * Check whether "new playlist" was created
    * Click options button
    * Click create new playlists option
    * Try to create playlist with default name
    * Check whether operations could not be performed
    * Change playlist name to "new playlist 2"
    * Check wheteher there are now two playlists

    *** Pass/Fail Criteria ***

    * Playlists cannot have same names
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create and check first playlist
    Tools.home.accept_new_playlist_modal()
    assert Tools.home.get_all_playlists_names() == ["new playlist"], \
        "First playlist was not created"

    # Try to create second playlist
    Tools.click_options_three_dots()
    Tools.click_options_create_new_playlist()
    Tools.home.accept_new_playlist_modal()
    assert Tools.home.is_new_playlist_button_visible(), \
        "Song with same name was accepted."
    Tools.home.update_text_new_playlist_name_edit_text_field("new playlist 2")
    Tools.home.accept_new_playlist_modal()

    # Check both playlists
    assert Tools.home.get_all_playlists_names() == ["new playlist",
                                                    "new playlist 2"], \
        "Second playlist was not created"


def test_modify_and_delete_playlist(Tools):
    """
    This test verifies whether app can properly handle modifying art cover
    of playlist and deleting playlsits.

    *** Test Procedure ***

    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Change playlist name to "new playlist 2"
    * Click create new playlists option
    * Long click on first playlist and choose "change aslbum cover"
    * Use "documents" and scroll to the bottom
    * Pick any picture
    * Make screenshot of both art covers
    * Compare screenshots - those should not match
    * Delete secodn playlist
    * Check number of playlists left
    * Delete first playlist
    * Check number of playlists left

    *** Pass/Fail Criteria ***

    * Playlists art covers are properly changed
    * Playlists are properly deleted
    """
    # Set up
    path_1 = "cover1.png"
    path_2 = "cover2.png"

    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create two playlists
    Tools.home.accept_new_playlist_modal()
    Tools.click_options_three_dots()
    Tools.click_options_create_new_playlist()
    Tools.home.update_text_new_playlist_name_edit_text_field("new playlist 2")
    Tools.home.accept_new_playlist_modal()

    # Change cover of first playlist
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_change_album_cover()
    Tools.click_documents()
    Tools.slide_down(4)
    assert Tools.click_last_picture()

    # Compare art covers
    covers = Tools.home.get_playlists_image_views()

    if os.path.isfile(path_1):
        os.remove(path_1)
    if os.path.isfile(path_2):
        os.remove(path_2)

    covers[0].screenshot(path_1)
    covers[1].screenshot(path_2)
    wait(3)

    with open(path_1, "rb") as f_1, open(path_2, "rb") as f_2:
        c_1 = f_1.read()
        c_2 = f_2.read()

    os.remove(path_1)
    os.remove(path_2)

    assert c_1 != c_2, "Art cover was not updated"

    # Delete playlists
    Tools.home.long_click_on_playlist("new playlist 2")
    Tools.home.click_delete_playlist()
    assert Tools.home.get_all_playlists_names() == ["new playlist"], \
        "Second playlist was not deleted"

    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_delete_playlist()
    assert Tools.home.get_all_playlists_names() == [], \
        "First playlist was not deleted"


def test_add_files_to_playlist(Tools):
    """
    This test verifies whether app can properly handle adding files to playlist.

    *** Test Procedure ***

    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Choose "add files to playlist"
    * Click "import from memory"
    * Choose "just once"
    * Chose single file
    * Check whether file was properly added to "file list"
    * Click "import from memory"
    * Choose "just once"
    * Chose two files
    * Check whether files were properly added to "file list"
    * Uncheck one file
    * Click "add selected to playlist"
    * Check whether two files were added to playlist

    *** Pass/Fail Criteria ***

    * Only two files were added to playlist
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create playlist
    Tools.home.accept_new_playlist_modal()

    # Import one file
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_add_files_to_playlist()
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([0])
    Tools.imported.click_load_file()

    # Check imported song
    assert Tools.imported.get_imported_songs_titles() == ["Iko Iko"]

    # Import two files
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([1, 2])
    Tools.imported.click_load_file()

    # Check imported songs
    assert Tools.imported.get_imported_songs_titles() == ["Castaways",
                                                          "Iko Iko",
                                                          "stukupuku (trailer)"]

    # Uncheck one file
    Tools.imported.click_check_boxes([1])

    # Add songs to playlist
    Tools.imported.click_add_selected_to_playlist()

    # Check added songs
    assert Tools.playlist.get_songs_titles() == ["Castaways",
                                                 "stukupuku (trailer)"]
    wait(3)


def test_queue_add_and_run(Tools):
    """
    This test verifies whether app can properly handle adding
    songs to queue and "skipping" songs in queue.
    *** Test Procedure ***
    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Choose "add files to playlist"
    * Click "import from memory"
    * Choose "just once"
    * Chose three files
    * Check whether files were properly added to "file list"
    * Click "add selected to playlist"
    * Click "start playlist"
    * Go to queue view
    * Check whether there are 3 songs
    * Click "skip" button three times
    * Check whether queue is empty

    *** Pass/Fail Criteria ***

    * Only two files were added to playlist
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create playlist
    Tools.home.accept_new_playlist_modal()

    # Import files
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_add_files_to_playlist()
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([0, 1, 2])
    Tools.imported.click_load_file()

    # Check imported songs
    assert Tools.imported.get_imported_songs_titles() == ["Castaways",
                                                          "Iko Iko",
                                                          "stukupuku (trailer)"]

    # Add songs to playlist
    Tools.imported.click_add_selected_to_playlist()

    # Check added songs
    assert Tools.playlist.get_songs_titles() == ["Castaways",
                                                 "Iko Iko",
                                                 "stukupuku (trailer)"]

    # Click "start playlist"
    Tools.playlist.click_start_playlist()

    # Go to queue view
    Tools.home.go_to_queue()

    # Check whether there are 3 songs
    song_act = Tools.queue.get_actual_song_title()
    songs = Tools.queue.get_songs_titles()
    assert [song_act] + songs == ["Castaways",
                                  "Iko Iko",
                                  "stukupuku (trailer)"]

    # Click "skip" button three times
    Tools.queue.click_fast_forward()
    Tools.queue.click_fast_forward()
    Tools.queue.click_fast_forward()

    # Check whether queue is empty
    assert Tools.queue.get_empty_queue_text() == 'your queue is empty,\nadd songs to queue\nfrom any playlist'


def test_queue_modify(Tools):
    """
    This test verifies whether app can properly handle modifying songs positions
    in queue.

    *** Test Procedure ***
    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Choose "add files to playlist"
    * Click "import from memory"
    * Choose "just once"
    * Chose three files
    * Click "add selected to playlist"
    * Click "start playlist"
    * Go to queue view
    * Check whether there are 3 songs
    * Move second from end song to end
    * Check queue
    * Move last song higher
    * Check queue
    * Open menu
    * Choose - clear queue
    * Check queue

    *** Pass/Fail Criteria ***

    * Only two files were added to playlist
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create playlist
    Tools.home.accept_new_playlist_modal()

    # Import files
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_add_files_to_playlist()
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([0, 1, 2])
    Tools.imported.click_load_file()

    # Check imported songs
    assert Tools.imported.get_imported_songs_titles() == ["Castaways",
                                                          "Iko Iko",
                                                          "stukupuku (trailer)"]

    # Add songs to playlist
    Tools.imported.click_add_selected_to_playlist()

    # Click "start playlist"
    Tools.playlist.click_start_playlist()

    # Go to queue view
    Tools.home.go_to_queue()

    # Check whether there are 3 songs
    song_act = Tools.queue.get_actual_song_title()
    songs = Tools.queue.get_songs_titles()
    assert [song_act] + songs == ["Castaways",
                                  "Iko Iko",
                                  "stukupuku (trailer)"]

    # Move second from end song to end
    Tools.queue.click_song_options(0)
    Tools.queue.click_move_song_bottom()

    # Check queue
    assert Tools.queue.get_songs_titles() == ["stukupuku (trailer)", "Iko Iko"]

    # Move last song higher
    Tools.queue.click_song_options(1)
    Tools.queue.click_move_song_top()

    # Check queue
    assert Tools.queue.get_songs_titles() == ["Iko Iko", "stukupuku (trailer)"]

    # Open menu
    Tools.click_options_three_dots()

    # Choose - clear queue
    Tools.home.click_clear_queue()

    # Check queue
    assert Tools.queue.get_empty_queue_text() == 'your queue is empty,\nadd songs to queue\nfrom any playlist'


def test_remove_file_from_file_list(Tools):
    """
    This test verifies whether app can properly handle removing song from file list.

    *** Test Procedure ***
    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Choose "add files to playlist"
    * Click "import from memory"
    * Choose "just once"
    * Chose three files
    * Click "add selected to playlist"
    * Click "add files"
    * Remove one file from list
    * Click "add files to playlist"
    * Check playlist songs

    *** Pass/Fail Criteria ***

    * Only two files were added to playlist
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create playlist
    Tools.home.accept_new_playlist_modal()

    # Import files
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_add_files_to_playlist()
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([0, 1, 2])
    Tools.imported.click_load_file()

    # Add songs to playlist
    Tools.imported.click_add_selected_to_playlist()

    # Click "add files"
    Tools.playlist.click_add_files()

    # Remove one file from list
    Tools.imported.long_click_song(0)
    Tools.imported.click_delete_song()

    # Click "add files to playlist"
    Tools.imported.click_add_selected_to_playlist()

    # Check playlist songs
    assert Tools.playlist.get_songs_titles() == ["Iko Iko",
                                                 "stukupuku (trailer)"]


def test_songs_forwarding(Tools):
    """
    This test verifies whether app can handle fast forwarding songs in song view.

    *** Test Procedure ***
    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Choose "add files to playlist"
    * Click "import from memory"
    * Choose "just once"
    * Chose three files
    * Click "add selected to playlist"
    * Click "start playlist"
    * Go to song view
    * Stop song
    * Chech actual song
    * Fast forward song
    * Stop song
    * Chech actual song
    * Fast forward song
    * Stop song
    * Chech actual song
    * Fast forward song
    * Stop song
    * Chech actual song

    *** Pass/Fail Criteria ***

    * Only two files were added to playlist
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create playlist
    Tools.home.accept_new_playlist_modal()

    # Import files
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_add_files_to_playlist()
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([0, 1, 2])
    Tools.imported.click_load_file()

    # Add songs to playlist
    Tools.imported.click_add_selected_to_playlist()

    # Click "start playlist"
    Tools.playlist.click_start_playlist()

    # Go to song view
    Tools.home.click_main_button()

    # Chech actual song
    assert Tools.song_view.get_actual_song_title() == "Castaways"

    # Fast forward song
    Tools.song_view.click_fast_forward()

    # Chech actual song
    assert Tools.song_view.get_actual_song_title() == "Iko Iko"

    # Fast forward song
    Tools.song_view.click_fast_forward()

    # Chech actual song
    assert Tools.song_view.get_actual_song_title() == "stukupuku (trailer)"


def test_queue_options(Tools):
    """
    This test verifies whether queue (playing) options work properly.

    *** Test Procedure ***
    * Open app
    * Accept access to storage
    * Accept modal window
    * Click options button
    * Choose "add files to playlist"
    * Click "import from memory"
    * Choose "just once"
    * Chose three files
    * Click "add selected to playlist"
    * Click "start playlist"
    * Go to song view
    * Change queue option to play whole queue in loop
    * Go to queue view
    * Click "fast forward" three times
    * Check queue state
    * Go to song view
    * Change queue option to loop single song
    * Go to queue view
    * Click "fast forward" two times
    * Check queue state

    *** Pass/Fail Criteria ***

    * Only two files were added to playlist
    """
    # Close storage pop up
    assert Tools.home.accept_access_to_storage()

    # Create playlist
    Tools.home.accept_new_playlist_modal()

    # Import files
    Tools.home.long_click_on_playlist("new playlist")
    Tools.home.click_add_files_to_playlist()
    Tools.imported.click_import_from_memory()
    Tools.imported.click_just_once()
    Tools.imported.click_one_mp3_files([0, 1, 2])
    Tools.imported.click_load_file()

    # Add songs to playlist
    Tools.imported.click_add_selected_to_playlist()

    # Click "start playlist"
    Tools.playlist.click_start_playlist()

    # Go to song view
    Tools.home.click_main_button()

    # Change queue option to play whole queue in loop
    Tools.song_view.click_repeat_button()

    # Go to queue view
    Tools.home.go_to_queue()

    # Click "fast forward" three times
    Tools.home.click_fast_forward()
    Tools.home.click_fast_forward()
    Tools.home.click_fast_forward()

    # Check queue state
    song_act = Tools.queue.get_actual_song_title()
    songs = Tools.queue.get_songs_titles()
    assert [song_act] + songs == ["Castaways",
                                  "Iko Iko",
                                  "stukupuku (trailer)"]

    # Go to song view
    Tools.home.click_main_button()

    # Change queue option to loop single song
    Tools.song_view.click_repeat_button()

    # Go to queue view
    Tools.home.go_to_queue()

    # Click "fast forward" two times
    Tools.home.click_fast_forward()
    Tools.home.click_fast_forward()

    # Check queue state
    song_act = Tools.queue.get_actual_song_title()
    songs = Tools.queue.get_songs_titles()
    assert [song_act] + songs == ["Castaways",
                                  "Iko Iko",
                                  "stukupuku (trailer)"]