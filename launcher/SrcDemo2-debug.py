import traceback

try:
    import SrcDemo2Launcher
    SrcDemo2Launcher.launch(True)
except:
    traceback.print_exc()
raw_input('Press Enter to close this window...')
