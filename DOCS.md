# Adding track information to your mod

> [!IMPORTANT]
> Versions of WAILT before *.3.0 download a track manifest from the GitHub repository. This manifest is no longer
> being updated to support new mods, and mod developers who wish to add compatibility with WAILT should not
> open PRs to modify it. The below instructions are applicable to versions *.3.0 or later.

WAILT is designed to work with tracks from other mods as well, and adding support is easy to do. To add your mod's
track information, simply include a file named `music.json` in the root of your mod's resource pack. WAILT embeds
a track manifest for Minecraft in its resources, which you can reference as an example.

## Structure of `music.json`

`music.json` has two top-level keys:

- `artists`: Metadata about artists.
- `tracks`: Names for tracks.

`artists` is a map of plain artist names to objects containing metadata about the artist. All of its keys are
optional. Currently, this object only has one key:

- `fancy_name`: A valid Minecraft [JSON text component](https://minecraft.wiki/w/Raw_JSON_text_format). If provided,
  this text component will be displayed
  as the name of this artist in toasts unless the user has disabled fancy names in the mod's config. If this key is not
  provided, or the user has disabled fancy names, the artist's key name in the `tracks` object will be used instead.

Artists are not required to have entries in this object.

`tracks` is a multi-layer map, laid out like so:

```json5
{
  "tracks": {
    "modid": {
      "Artist Name": {
        "track_location": "Track Name"
      }
    }
  }
}
```

where:

- `"modid"`: the mod ID of the mod which adds the music.
- `"Artist Name"`: the _human-readable_ name of the artist, which will also be used to look the artist up in `artists`
  and shown in toasts if the artist does not have a `fancy_name`.
- `"track_location"`: the path to the audio file relative to `/assets/<modid>/sounds/music`. Files which are not in the
  `music` subdirectory will be ignored by WAILT.
- `"Track Name"`: The human-readable name of the track, as a JSON text component.

## Example `music.json`

```json5
{
  "artists": {
    "Artist Name": {
      "component": "Artist Name but fancy"
    }
  },
  "tracks": {
    "examplemod": {
      "Artist Name": {
        "game/epic_track": "Epic track"
      }
    }
  }
}
```
