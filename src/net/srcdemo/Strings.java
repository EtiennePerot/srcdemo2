package net.srcdemo;

public interface Strings {
	static final String aboutUnknownVersion = "Unknown";
	static final String audioOptBuffered = "WAV (Buffered)";
	static final String audioOptBufferedExplanation = "The .wav audio file will be written to disk in chunks.\nGood to get a .wav file without overusing the disk.";
	static final String audioOptDisabled = "Disabled (video only)";
	static final String audioOptDisabledExplanation = "When audio is disabled, no audio processing occurs.\nGood if you are rendering video at a high framerate,\nas the sound file is likely to be off-sync.\nIt is then wise to turn audio off, then do another render\nwith audio enabled and video disabled.";
	static final String audioOptDisk = "WAV (Straight to disk)";
	static final String audioOptDiskExplanation = "The .wav audio file will directly be written to disk.\nGood if you\'re not feeling fancy.";
	static final String audioOptFlac = "FLAC";
	static final String audioOptFlacExplanation = "FLAC is a lossless, compressed sound format.\nGood for losslessness while keeping disk usage minimal.";
	static final String audioOptVorbis = "Ogg Vorbis";
	static final String audioOptVorbisExplanation = "Vorbis is a lossy, compressed sound format.\nGood if running low on disk space.";
	static final String btnActivate = "Activate";
	static final String btnBrowse = "Browse...";
	static final String btnDeactivate = "Deactivate";
	static final String btnQuit = "Quit";
	static final String btnRenderAudioBufferFlush = "Flush";
	static final String btnRenderAudioBufferFlushed = "Flushed";
	static final String btnRenderAudioBufferFlushing = "Flushing";
	static final String btnUpdateCheck = "Check for updates";
	static final String btnUpdateChecking = "Checking...";
	static final String btnUpdateRecheck = "Re-check for updates";
	static final String chkUpdateAutoCheck = "Check for updates when starting";
	static final String cmdBlendRate1 = "Blend rate is ";
	static final String cmdBlendRate2 = "Multiply this by the desired video framerate to get the\nhost_framerate to use in-game.";
	static final String cmdDefaultPrefix = "Def: ";
	static final String cmdDisplayAudio1 = "[Audio: ";
	static final String cmdDisplayAudio2 = "/";
	static final String cmdDisplayAudio3 = " KB (";
	static final String cmdDisplayAudio4 = "%)]";
	static final String cmdDisplayAudioDestroyed = "[Audio: Terminated]";
	static final String cmdDisplayAudioFlushing = "[Audio: Flushing...]";
	static final String cmdDisplayAudioWaiting = "[Audio: Waiting]";
	static final String cmdDisplayVideo1 = "[Video: ";
	static final String cmdDisplayVideo2 = " frames/";
	static final String cmdDisplayVideo3 = " fps/";
	static final String cmdDisplayVideo4 = " saved] ";
	static final String cmdEnumPossibleValues = "Possible values:";
	static final String cmdGoingToMount = "Will now mount virtual filesystem to:";
	static final String cmdHostFramerate = "host_framerate ";
	static final String cmdMustIncludeRenderOptions = "Must specify at least -m and -o.\n\nUse -h for help.";
	static final String cmdToExit = "To exit, do NOT kill the SrcDemo2 wrapper process;\nkill the java process instead.";
	static final String cmdVersionPrefix = "Version: ";
	static final String dateSeparator = "-";
	static final String errCmdInvalidAudioArgument = "Invalid audio output type";
	static final String errCmdInvalidVideoArgument = "Invalid video output type.";
	static final String errDirectoriesEqual = "Directories must not be equal";
	static final String errDokanNotInstalled = "Dokan is not installed.\n\nMore information will be available if you start in debug mode.";
	static final String errDokanTitle = "Dokan error";
	static final String errInvalidBacking = "Invalid folder to save frames.";
	static final String errInvalidDokan = "The version of Dokan installed is not the correct one.\nThis application required Dokan 0.6.";
	static final String errInvalidMountpoint = "Invalid folder where the game will save frames.";
	static final String errMountpointNotEmpty = "Folder where the game will save frames is not empty.";
	static final String errUpdateContact = "Error while attempting to grab update information.";
	static final String errUpdateInvalidVersion = "Cannot check for updates: This build's version is unknown.";
	static final String errUpdateReading = "Error while reading update information.";
	static final String grpRenderingAudioBuffer = "Audio buffer";
	static final String grpRenderingVideoFrames = "Video frames";
	static final String lblAboutArt = "Artwork (logo, icon) by Mythee.";
	static final String lblAboutMain = "SrcDemo² - By <a href=\"https://perot.me/\">Etienne Perot</a>";
	static final String lblAudioBufferSize = "Buffer size:";
	static final String lblAudioBufferTimeout = "Buffer timeout:";
	static final String lblAudioType = "Audio output:";
	static final String lblBlendRate = "Motion blur blend rate: ";
	static final String lblBufferWarning = "When using a buffer, remember to press \"Deactivate\" at the end of the recording, after having entered \"endmovie\".\nOtherwise, the audio file may be corrupt.";
	static final String lblBuildDate = "Build date:";
	static final String lblClientJvmWarning = "<strong>Note</strong>: For better performance, <a href=\"http://code.google.com/p/srcdemo2/wiki/ServerJVM\">use a server JVM</a>.";
	static final String lblDefaultDownloadLink = "http://code.google.com/p/srcdemo2/downloads/list";
	static final String lblEffectiveFps = "Effective recording FPS: ";
	static final String lblEnablePreview = "Enable preview (unstable)";
	static final String lblFramesPerSecond = "Frames processed per second:";
	static final String lblFramesProcessedPerSecond = "Frames per second:";
	static final String lblFramesProcessedPerSecondDefault = "...";
	static final String lblFramesProcessedPerSecondFormat = "#.00";
	static final String lblGaussianBlending = "Use Gaussian blending";
	static final String lblGaussianVariance = "Gaussian variance:";
	static final String lblInvalidSettings = "Invalid settings: ";
	static final String lblJpegQuality = "JPEG quality";
	static final String lblLastFrameProcessed = "Last frame processed: ";
	static final String lblLastFrameProcessedDefault = "N/A";
	static final String lblLastFrameSaved = "Last frame saved: ";
	static final String lblLastFrameSavedDefault = "N/A";
	static final String lblMakeSureFramerate = "Make sure to set host_framerate before you start rendering!";
	static final String lblPressWhenReady = "Press \"Activate\" when ready.";
	static final String lblReadyToRender1 = "Ready to render. Source engine framerate: ";
	static final String lblReadyToRender2 = " fps.";
	static final String lblReadyToRenderNoVideo = "Ready to render.";
	static final String lblRenderAudioBuffer1 = " KB (";
	static final String lblRenderAudioBuffer2 = "%)";
	static final String lblRenderAudioBuffer3 = "(";
	static final String lblRenderAudioBuffer4 = " KB total)";
	static final String lblRenderAudioBufferClosed = "Inactive.";
	static final String lblRenderAudioBufferWriting = "Writing...";
	static final String lblRenderAudioBufferWritten = "Written.";
	static final String lblShutterAngle = "Simulated <a href=\"http://code.google.com/p/srcdemo2/wiki/ShutterAngle\">shutter angle</a>: ";
	static final String lblStoat0 = "<center><big><strong>Salute the Secret Stoat!</strong></big></center>";
	static final String lblStoat1 = "Praise the <strong>Secret Stoat</strong> and all it stands for: <strong>WIN</strong>.";
	static final String lblStoat2 = "<strong>Definitions of win on the Web:</strong>";
	static final String lblStoat3 = "- be the winner in a contest or competition; be victorious; \"He won the Gold Medal in skating\"; \"Our home team won\"; \"Win the game\"";
	static final String lblStoat4 = "- acquire: win something through one's efforts; \"I acquired a passing knowledge of Chinese\"; \"Gain an understanding of international finance\"";
	static final String lblStoat5 = "- gain: obtain advantages, such as points, etc.; \"The home team was gaining ground\"";
	static final String lblStoat6 = "- a victory (as in a race or other competition); \"he was happy to get the win\"";
	static final String lblStoat7 = "- winnings: something won (especially money)";
	static final String lblStoat8 = "- succeed: attain success or reach a desired goal; \"The enterprise succeeded\"; \"We succeeded in getting tickets to the show\"; \"she struggled to overcome her handicap and won\"";
	static final String lblStoatTitle = "The Secret Stoat";
	static final String lblTargetFps = "Final video FPS: ";
	static final String lblTgaCompressionRLE = "TGA RLE compression";
	static final String lblUpdateChecking = "Checking for updates...";
	static final String lblUpdateIsUpToDate = "SrcDemo² is up to date.";
	static final String lblUpdateNewVersion = "New version available: ";
	static final String lblUpdateNewVersionNoLink = " (Specific download link not found)";
	static final String lblVideoType = "Video output:";
	static final String lblVorbisQuality = "Vorbis quality";
	static final String lblVorbisQualityPrefix = "q";
	static final String productName = "SrcDemo²";
	static final String spnAudioBufferSize = " kilobytes";
	static final String spnAudioBufferTimeout = " seconds";
	static final String spnBlendRatePlural = " frames per frame";
	static final String spnBlendRateSingular = " frame (identity)";
	static final String spnShutterAnglePlural = " degrees";
	static final String spnShutterAngleSingular = " degree";
	static final String spnTargetFpsPlural = " frames per second";
	static final String spnTargetFpsSingular = " frame per second";
	static final String step1 = "1. Select the folder where the game will save frames.";
	static final String step1Dialog = "Select the folder where you want the final frames to be saved.";
	static final String step2 = "2. Select the folder where you want the final frames to be saved.\n(Must be a different directory!)";
	static final String step2Dialog = "Select the folder where the game will save frames.";
	static final String step3 = "3. Set parameters.";
	static final String tabAbout = "About/Updates";
	static final String tabAudio = "Audio";
	static final String tabRender = "Rendering";
	static final String tabVideo = "Video";
	static final String titleBuildPrefix = " - Build ";
	static final String urlHomepage = "http://srcdemo2.googlecode.com/";
	static final String urlUpdateFeed = "http://code.google.com/feeds/p/srcdemo2/downloads/basic";
	static final String videoOptDisabled = "Disabled (Audio only)";
	static final String videoOptDisabledExplanation = "When video is disabled, the video framerate doesn't matter.\nHowever, it is recommended to record at\nmore than 66 frames to avoid a bug in Source Recorder\ncausing some sounds to not be played at all when playing\na demo at a low framerate compared to the game's tickrate.";
	static final String videoOptJpg = "JPEG";
	static final String videoOptJpgExplanation = "JPEG is a lossy, compressed image format.\nGood if running low on disk space.";
	static final String videoOptPng = "PNG";
	static final String videoOptPngExplanation = "PNG is a lossless, compressed image format.\nGood for losslessness while keeping disk usage minimal.";
	static final String videoOptTga = "TGA";
	static final String videoOptTgaExplanation = "TGA is a lossless, uncompressed image format.\nGood if the disk is very fast but the CPU isn't.";
}