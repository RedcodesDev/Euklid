package dev.redcodes.euklid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.redcodes.euklid.data.token.Token;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Euklid {

	private static JDA jda;

	private static Logger logger = LoggerFactory.getLogger(Euklid.class);

	private static String version = "Pre-Release 1.0";

	private static boolean dev = false;

	private static String year = "2021";

	private static String icon = "https://i.imgur.com/O74dUFG.jpg";

	private static Instant online = Instant.now();

	public static void main(String[] args) {

		String token = Token.getToken();

		JDABuilder builder = JDABuilder.createDefault(token);

		builder.setActivity(Activity.watching("Bot starting..."));
		builder.setStatus(OnlineStatus.IDLE);

		List<GatewayIntent> intents = new ArrayList<>();
		intents.addAll(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
		intents.remove(GatewayIntent.GUILD_PRESENCES);

		builder.setEnabledIntents(intents);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);

		try {
			jda = builder.build();
		} catch (LoginException e) {
			e.printStackTrace();
		}

		year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

		logger.info("The bot is now online!");

		shutdown();
		runLoop();
	}

	private static boolean shutdown = false;

	private static void shutdown() {

		new Thread(() -> {

			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			try {
				while ((line = reader.readLine()) != null) {
					if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("stop")) {
						shutdown = true;
						if (jda != null) {
							jda.getPresence().setStatus(OnlineStatus.OFFLINE);
							jda.shutdown();
							logger.info("The bot is now offline!");
						}
						reader.close();
						System.exit(0);
						break;

					} else {
						logger.warn("Unknown Command \"" + line + "\"");
					}
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}).start();
	}

	private static void runLoop() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (!shutdown) {
					onSecond();
				}
			}
		}, 0, (7 * 1000));
	}

	private static boolean commandCheck = true;
	private static String[] status = new String[] { "axolotl groans", "%members% User", "%version%" };
	private static Random rand = new Random();

	private static void onSecond() {

		if (commandCheck) {
			commandCheck = false;

			List<CommandData> cmds = new ArrayList<>();

			if (dev) {
				jda.getGuildById(580732235313971211L).updateCommands().addCommands(cmds)
						.queue(commands -> logger.info("Commands published"));
			} else {
				jda.updateCommands().addCommands(cmds).queue(commands -> logger.info("Commands published"));
			}

		}

		int i = rand.nextInt(status.length);

		int users = 0;

		for (Guild guild : jda.getGuilds()) {
			users = users + guild.getMemberCount();
		}

		String text = status[i].replace("%members%", String.valueOf(users)).replace("%version%", version)
				.replace("%guilds%", String.valueOf(jda.getGuilds().size()));

		if (!jda.getPresence().getActivity().getName().equals(text)) {

			if (!dev) {
				if (text.contains("axolotl")) {
					jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening(text));
				} else if (text.contains("User") || text.contains("Guilds") || text.contains("axolotl")) {
					jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching(text));
				} else {
					jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(text));
				}
			} else {
				jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.watching("Development"));
			}
		} else {
			onSecond();
		}

	}

}