using System.Text.Json;
using System.Text.Json.Nodes;
using Anchor.Models;

namespace Anchor.Data;

/// <summary>Exports/imports the same JSON shape the prototype's backup file and the Android app use.</summary>
public static class Backup
{
    private static readonly JsonSerializerOptions WriteOptions = new() { WriteIndented = true };

    public static string Export(AppData data)
    {
        var payload = new JsonObject
        {
            ["suppliers"] = new JsonArray(data.Suppliers.Select(s => (JsonNode)new JsonObject
            {
                ["id"] = s.Id, ["name"] = s.Name, ["contact"] = s.Contact, ["notes"] = s.Notes,
            }).ToArray()),
            ["categories"] = new JsonArray(data.Categories.Select(c => (JsonNode)JsonValue.Create(c)!).ToArray()),
            ["budgets"] = new JsonArray(data.Budgets.Select(b => (JsonNode)new JsonObject
            {
                ["category"] = b.Category, ["amount"] = b.Amount,
            }).ToArray()),
            ["purchases"] = new JsonArray(data.Purchases.Select(p => (JsonNode)new JsonObject
            {
                ["id"] = p.Id, ["item"] = p.Item, ["category"] = p.Category, ["sup"] = p.SupplierId,
                ["qty"] = p.Qty, ["unit"] = p.Unit, ["price"] = p.Price, ["date"] = p.Date,
                ["status"] = p.Status, ["notes"] = p.Notes,
                ["basis"] = p.Basis.HasValue ? JsonValue.Create(p.Basis.Value) : null,
                ["groupId"] = p.GroupId,
                ["docs"] = new JsonArray(p.Docs.Select(d => (JsonNode)new JsonObject
                {
                    ["name"] = d.Name, ["type"] = d.Type, ["date"] = d.Date,
                }).ToArray()),
            }).ToArray()),
            ["groups"] = new JsonArray(data.Groups.Select(g => (JsonNode)new JsonObject
            {
                ["id"] = g.Id, ["title"] = g.Title, ["qty"] = g.Qty, ["unit"] = g.Unit,
                ["category"] = g.Category, ["status"] = g.Status,
                ["selected"] = g.SelectedQuoteId, ["purchaseId"] = g.PurchaseId,
                ["quotes"] = new JsonArray(g.Quotes.Select(q => (JsonNode)new JsonObject
                {
                    ["id"] = q.Id, ["sup"] = q.SupplierId, ["price"] = q.Price, ["valid"] = q.ValidUntil, ["status"] = q.Status,
                }).ToArray()),
            }).ToArray()),
            ["reminders"] = new JsonArray(data.Reminders.Select(r => (JsonNode)new JsonObject
            {
                ["id"] = r.Id, ["title"] = r.Title,
                ["link"] = new JsonObject { ["t"] = r.LinkType, ["id"] = r.LinkId },
                ["due"] = r.Due, ["status"] = r.Status,
            }).ToArray()),
        };

        var root = new JsonObject
        {
            ["app"] = "anchor-procurement",
            ["version"] = 3,
            ["exportedAt"] = Format.Today(),
            ["data"] = payload,
        };

        return root.ToJsonString(WriteOptions);
    }

    public static AppData? Restore(string json)
    {
        try
        {
            var root = JsonNode.Parse(json)!.AsObject();
            if ((string?)root["app"] != "anchor-procurement") return null;
            var data = root["data"]!.AsObject();

            var result = new AppData();

            foreach (var s in data["suppliers"]!.AsArray())
            {
                result.Suppliers.Add(new Supplier { Id = (string)s!["id"]!, Name = (string)s["name"]!, Contact = (string?)s["contact"] ?? "", Notes = (string?)s["notes"] ?? "" });
            }

            foreach (var c in data["categories"]!.AsArray())
            {
                result.Categories.Add((string)c!);
            }

            foreach (var b in data["budgets"]!.AsArray())
            {
                result.Budgets.Add(new Budget { Category = (string)b!["category"]!, Amount = (double)b["amount"]! });
            }

            foreach (var p in data["purchases"]!.AsArray())
            {
                var docs = new List<Doc>();
                var docsNode = p!["docs"]?.AsArray();
                if (docsNode != null)
                {
                    foreach (var d in docsNode)
                    {
                        docs.Add(new Doc { Name = (string)d!["name"]!, Type = (string)d["type"]!, Date = (string)d["date"]! });
                    }
                }

                result.Purchases.Add(new Purchase
                {
                    Id = (string)p["id"]!, Item = (string)p["item"]!, Category = (string)p["category"]!, SupplierId = (string)p["sup"]!,
                    Qty = (double)p["qty"]!, Unit = (string)p["unit"]!, Price = (double)p["price"]!, Date = (string)p["date"]!,
                    Status = (string)p["status"]!, Notes = (string?)p["notes"] ?? "",
                    Basis = p["basis"] is JsonValue bv ? (double?)bv.GetValue<double>() : null,
                    GroupId = (string?)p["groupId"],
                    Docs = docs,
                });
            }

            foreach (var g in data["groups"]!.AsArray())
            {
                var quotes = new List<Quote>();
                foreach (var q in g!["quotes"]!.AsArray())
                {
                    quotes.Add(new Quote { Id = (string)q!["id"]!, GroupId = (string)g["id"]!, SupplierId = (string)q["sup"]!, Price = (double)q["price"]!, ValidUntil = (string)q["valid"]!, Status = (string)q["status"]! });
                }
                result.Groups.Add(new QuoteGroup
                {
                    Id = (string)g["id"]!, Title = (string)g["title"]!, Qty = (double)g["qty"]!, Unit = (string)g["unit"]!,
                    Category = (string)g["category"]!, Status = (string)g["status"]!,
                    SelectedQuoteId = (string?)g["selected"], PurchaseId = (string?)g["purchaseId"], Quotes = quotes,
                });
            }

            foreach (var r in data["reminders"]!.AsArray())
            {
                var link = r!["link"]!.AsObject();
                result.Reminders.Add(new Reminder { Id = (string)r["id"]!, Title = (string)r["title"]!, LinkType = (string)link["t"]!, LinkId = (string)link["id"]!, Due = (string)r["due"]!, Status = (string)r["status"]! });
            }

            result.Settings = new Settings();
            return result;
        }
        catch
        {
            return null;
        }
    }
}
