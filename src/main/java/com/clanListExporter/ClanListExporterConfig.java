package com.clanListExporter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ClanListExporter")
public interface ClanListExporterConfig extends Config {
	@ConfigItem(position = 1, keyName = "fullClanChat", name = "Include offline members", description = "When exporting clan chat, optionally export offline members")
	default boolean fullClanChat() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "CSVMode", name = "Export in CSV format", description = "Export the list in CSV format")
	default boolean csvMode() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "timestamp", name = "Timestamp export file", description = "Include timestamp in exported file name")
	default boolean timestamp() {
		return true;
	}
}
