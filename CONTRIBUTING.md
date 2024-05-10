# Contributing track information

WAILT is designed to work with tracks from other mods as well, and adding support is easy to do.
Track information is stored in a metadata file which the mod downloads at startup, so your contribution will make
the experience better for all users.

The track metadata is located
in [src/main/resources/tracks.json](https://github.com/GlyphMods/wailt/blob/1.20.6/src/main/resources/tracks.json),
which WAILT will automatically re-download at startup if possible. (Failing that, it also caches the
file on disk and will use a copy inside the JAR as a last resort.)

To contribute track information for a mod, open a pull request with the relevant data added to `tracks.json`, as
described below. If you need help, ping Ginger (`gingershaped`) in
the [Neoforge discord](https://discord.neoforged.net), or send them a DM.

## Structure of `tracks.json`

`tracks.json` has three top-level keys:

- `version`: The format version of the file; see the section on version history.
- `artists`: Metadata about artists.
- `tracks`: Names for tracks.

`artists` is a map of artist names to objects containing metadata about the artist. Currently,
this object only has one key:

- `component`: A valid Minecraft [JSON text component](https://minecraft.wiki/w/Raw_JSON_text_format);
  all types (list/object/string) are accepted. If this value is not provided, WAILT will use the artist's key
  in `tracks`.

Artists are not required to have entries in this object; WAILT will use reasonable defaults if one does not.

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

- `"modid"`: the mod ID of the mod which adds the music.
- `"Artist Name"`: the _human-readable_ name of the artist, which will also be used to look the artist up in `artists`.
- `"track_location"`: the path to the audio file relative to `/assets/<modid>/sounds`, with a prefix of `music/`
  stripped
  if applicable. See below for examples.
- `"track Name"`: The human-readable name of the track.

## Example song names

```json5
{
  "/assets/mymod/sounds/music.ogg": "music",
  "/assets/mymod/sounds/music/epic.ogg": "epic",
  "/assets/mymod/sounds/custom-music/funky.ogg": "custom-music/funky",
  "/assets/mymod/sounds/music/game/funky.ogg": "game/funky"
  // _not_ "music/game/funky", since "music/" is stripped
}
```

## Example `tracks.json`

```json5
{
  "version": 1,
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

## Version history

- `0`: Initial version during mod development
- `1`: Added the `artists` key to support JSON text components as artist names.