package edu.uchicago.gerber.mvc.controller;


import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SoundLoader {


	/* A Looped clip is one that plays for an indefinite time until you call the .stopSound() method. Non-looped
		clips, which may have multiple instances that play concurrently, must be queued onto the ThreadPoolExecutor
		below. Make sure to place all sounds directly in the src/main/resources/sounds directory and suffix any looped
		clips with _loop.
	 */
	private static final Map<String, Clip> LOOPED_CLIPS_MAP;

	// Load all looping sounds in the static context.
	static {
		Path rootDirectory = Paths.get("src/main/resources/sounds");
		Map<String, Clip> localMap = null;
		try {
			localMap = loadLoopedSounds(rootDirectory);
		} catch (IOException e) {
			e.fillInStackTrace();
			throw new ExceptionInInitializerError(e);
		}
		LOOPED_CLIPS_MAP = localMap;

	}

	/* ThreadPoolExecutor for playing non-looped sounds. Limit the number of threads to 5 at a time. Sounds that can
	be played simultaneously, must be queued onto the soundExecutor at runtime.
	 */
	private static final ThreadPoolExecutor soundExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

	private static boolean loopedCondition(String str){
		str = str.toLowerCase();
		return str.endsWith("_loop.wav");
	}

	private static Map<String, Clip> loadLoopedSounds(Path rootDirectory) throws IOException {
		Map<String, Clip> soundClips = new HashMap<>();
		Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (loopedCondition(file.toString())) {
					try {
						Clip clip = getLoopClip(file);
						if (clip != null) {
							soundClips.put(file.getFileName().toString(), clip);
						}
					} catch (Exception e) {
						e.fillInStackTrace();
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				exc.fillInStackTrace();
				return FileVisitResult.CONTINUE;
			}
		});
		return soundClips;
	}

	private static Clip getLoopClip(Path fileName) throws Exception {
		Clip clip = null;
		try {
			// Adjust the path to be relative to the resources directory
			String relativePath = "/sounds/" + fileName.getFileName().toString();
			InputStream audioSrc = SoundLoader.class.getResourceAsStream(relativePath);

			if (audioSrc == null) {
				throw new IOException("No such sound file exists at " + relativePath);
			}

			InputStream bufferedIn = new BufferedInputStream(audioSrc);
			AudioInputStream aisStream = AudioSystem.getAudioInputStream(bufferedIn);
			clip = AudioSystem.getClip();
			clip.open(aisStream);

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.fillInStackTrace();
			throw e;
		}

		return clip;
	}



	// Used for both looped and non-looped clips
	public static void playSound(final String strPath) {
		//Looped clips are fetched from existing static LOOP_SOUNDS_MAP at runtime.
		if (loopedCondition(strPath)){
			try {
				LOOPED_CLIPS_MAP.get(strPath).loop(Clip.LOOP_CONTINUOUSLY);
			} catch (Exception e){
				//catch any exception and continue.
				e.fillInStackTrace();
			}
			return;
		}
        //Non-looped clips are enqueued onto executor-threadpool at runtime.
		soundExecutor.execute(new Runnable() {
			public void run() {
				try {
					Clip clp = AudioSystem.getClip();

					InputStream audioSrc = SoundLoader.class.getResourceAsStream("/sounds/" + strPath);
                    assert audioSrc != null;
                    InputStream bufferedIn = new BufferedInputStream(audioSrc);
					AudioInputStream aisStream = AudioSystem.getAudioInputStream(bufferedIn);

					clp.open(aisStream);
					clp.start();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		});

	}

	//Non-looped clips can not be stopped, they simply expire on their own. Calling this method on a
	// non-looped clip will do nothing.
	public static void stopSound(final String strPath) {
		if (!loopedCondition(strPath)) return;
		try {
			LOOPED_CLIPS_MAP.get(strPath).stop();
		} catch (Exception e){
			//catch any exception and continue.
			e.fillInStackTrace();
		}
	}







}
