package helio.providers.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.google.gson.JsonObject;

import helio.blueprints.AsyncDataProvider;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableEmitter;

public class FileWatcherProvider implements AsyncDataProvider {//, Consumer<Emitter<String>> {

	private File file;
	
	@Override
	public void configure(JsonObject configuration) {
		if(configuration.has("file")) {
			this.file = new File(configuration.get("file").toString());
		}else {
			throw new IllegalArgumentException("Provide a json configuration with \"file\" key");
		}

	}

	@Override
	public void subscribe(@NonNull FlowableEmitter<@NonNull String> emitter) throws Throwable {
		final Path path = FileSystems.getDefault().getPath(file.getPath());
		try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
			final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			while (true) {
				final WatchKey wk = watchService.take();
				for (WatchEvent<?> event : wk.pollEvents()) {
					// we only register "ENTRY_MODIFY" so the context is always a Path.
					final Path changed = (Path) event.context();
					if (changed.endsWith(file.getName())) {
						BufferedReader br = new BufferedReader(new FileReader(file));
						String st;
						StringBuilder sbr = new StringBuilder();
						while ((st = br.readLine()) != null)
							sbr.append(st).append("\n");
						br.close();
						emitter.onNext(sbr.toString().trim());
						
					}
				}
				boolean valid = wk.reset();
				if (!valid) {
					System.out.println("Key has been unregisterede");
				}
			}
		}catch(Exception e) {
			emitter.onError(e);
		}
	}




}
