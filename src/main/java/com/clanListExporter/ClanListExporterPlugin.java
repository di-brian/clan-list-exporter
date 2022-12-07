package com.clanListExporter;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.clan.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
@PluginDescriptor(
		name = "Clan List Exporter"
)
public class ClanListExporterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClanListExporterConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private MenuManager menuManager;

	@Provides
	ClanListExporterConfig getConfig(ConfigManager configManager) {
		return (ClanListExporterConfig) configManager.getConfig(ClanListExporterConfig.class);
	}

	public static final File EXPORT_DIR = new File(RUNELITE_DIR, "clanlistexports");

	@Override
	protected void startUp() throws Exception
	{
		addExportMenuItem();
	}

	@Override
	protected void shutDown() throws Exception
	{
		removeExportMenuItem();
	}

	private static final WidgetMenuOption FRIENDS_CHAT;
	private static final WidgetMenuOption CLAN_CHAT;

	static {
		FRIENDS_CHAT = new WidgetMenuOption("Export", "Friends Chat", 46333955);
		CLAN_CHAT = new WidgetMenuOption("Export", "Clan Chat", 46333956);
	}

	private void addExportMenuItem() {
		this.menuManager.addManagedCustomMenu(FRIENDS_CHAT, null);
		this.menuManager.addManagedCustomMenu(CLAN_CHAT, null);
	}

	private void removeExportMenuItem() {
		this.menuManager.removeManagedCustomMenu(FRIENDS_CHAT);
		this.menuManager.removeManagedCustomMenu(CLAN_CHAT);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) throws Exception {
		if(Text.removeTags(event.getMenuTarget()).equals("Friends Chat")){
			exportList(ChatType.FRIENDS_CHAT);
		}
		if(Text.removeTags(event.getMenuTarget()).equals("Clan Chat")){
			exportList(ChatType.CLAN_CHAT);
		}
	}

	public void exportList(ChatType chatType){
		if(client.getGameState() == GameState.LOGGED_IN) {
			List<String> listToWrite = getMembersList(chatType);
			if(!listToWrite.isEmpty()) {
				writeToFile(listToWrite, chatType, config.csvMode(), config.timestamp());
			}
		}
	}

	public List<String> getMembersList(ChatType chatType){

		ArrayList<String> memberList = new ArrayList<>();

		if(chatType == ChatType.CLAN_CHAT) {
			if (client.getClanChannel() != null) {

				ClanSettings clanSettings = this.client.getClanSettings();

				if (config.fullClanChat()) {
					for (ClanMember clanMember : clanSettings.getMembers()) {
						memberList.add(Text.toJagexName(clanMember.getName()));
					}
				} else {
					for (ClanChannelMember clanMember : this.client.getClanChannel().getMembers()) {
						memberList.add(Text.toJagexName(clanMember.getName()));
					}
				}
			} else {
				chatMessageManager.queue(
						QueuedMessage.builder()
								.type(ChatMessageType.CONSOLE)
								.runeLiteFormattedMessage("Please enter a clan chat before trying to export!.")
								.build());
			}
		}

		if (chatType == ChatType.FRIENDS_CHAT){
			if (client.getFriendsChatManager().getOwner() != null){
				for (FriendsChatMember friendsChatMember : (FriendsChatMember[]) this.client.getFriendsChatManager().getMembers()) {
						memberList.add(Text.toJagexName(friendsChatMember.getName()));
					}
			}else{
				chatMessageManager.queue(
						QueuedMessage.builder()
								.type(ChatMessageType.CONSOLE)
								.runeLiteFormattedMessage("Please enter a friends chat before trying to export!.")
								.build());
			}
		}
		return memberList;
	}

	public void writeToFile(List<String> list, Enum chatType, boolean csvMode, boolean timestamp){

		String fileName = chatType == ChatType.FRIENDS_CHAT? client.getFriendsChatManager().getName() : client.getClanChannel().getName();
		String fileType = csvMode? ".csv" : ".txt";

		if(timestamp) {
			Date date = new Date();
			SimpleDateFormat DateFor = new SimpleDateFormat("dd-MM-yyyy");
			String stringDate = DateFor.format(date);
			fileName = fileName.concat("-").concat(stringDate);
		}

		try {
			if (!EXPORT_DIR.exists()){
				EXPORT_DIR.mkdir();
			}

			FileWriter fw = new FileWriter(EXPORT_DIR + "/" + fileName + fileType);
			for (String name : list)
				if(config.csvMode()) {
					fw.write(name + ",");
				}else {
					fw.write(name + "\r\n");
				}
			fw.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}
