package helio.providers.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonObject;

import helio.blueprints.DataProvider;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.FlowableEmitter;


public class FileProvider implements DataProvider {

	private String file;

	@Override
	public void configure(JsonObject configuration) {
		if (configuration.has("file")) {
			this.file = configuration.get("file").getAsString();
		} else {
			throw new IllegalArgumentException("Provide a json configuration with \"file\" key");
		}
	}

	@Override
	public void subscribe(@NonNull FlowableEmitter<@NonNull String> emitter) throws Throwable {
			try {
				String sbr=readFile(file); 
				emitter.onNext(sbr.toString());
				emitter.onComplete();
			}catch(Exception e) {
				emitter.onError(e);
			}
	}

	public static String readFile(String fileName) throws IOException {
		StringBuilder data = new StringBuilder();
		// 1. Read the file
		
			FileReader file = new FileReader(fileName);
			BufferedReader bf = new BufferedReader(file);
			// 2. Accumulate its lines in the data var
			bf.lines().forEach(line -> data.append(line).append("\n"));
			bf.close();
			file.close();
		
		return data.toString();
	}


}
