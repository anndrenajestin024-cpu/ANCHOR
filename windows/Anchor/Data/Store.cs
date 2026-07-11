using System.Text.Json;
using Anchor.Models;

namespace Anchor.Data;

/// <summary>Local-only JSON file store (no backend), mirroring the prototype's localStorage persistence.</summary>
public class Store
{
    private static readonly JsonSerializerOptions JsonOptions = new() { WriteIndented = true };

    private readonly string _filePath;

    public Store()
    {
        var dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Anchor");
        Directory.CreateDirectory(dir);
        _filePath = Path.Combine(dir, "anchor-data.json");
    }

    public AppData Load()
    {
        if (!File.Exists(_filePath))
        {
            var seeded = Seed.Full();
            Save(seeded);
            return seeded;
        }

        try
        {
            var json = File.ReadAllText(_filePath);
            var data = JsonSerializer.Deserialize<AppData>(json);
            return data ?? Seed.Full();
        }
        catch
        {
            return Seed.Full();
        }
    }

    public void Save(AppData data)
    {
        var json = JsonSerializer.Serialize(data, JsonOptions);
        File.WriteAllText(_filePath, json);
    }
}
