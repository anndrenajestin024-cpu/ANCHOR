package com.anchor.procurement.data

import org.json.JSONArray
import org.json.JSONObject

/** Exports the in-memory AnchorData snapshot to the same JSON shape the prototype's backup file uses. */
object Backup {
    fun export(data: AnchorData): String {
        val root = JSONObject()
        root.put("app", "anchor-procurement")
        root.put("version", 3)
        root.put("exportedAt", Format.today())

        val payload = JSONObject()

        payload.put("suppliers", JSONArray().apply {
            data.suppliers.forEach {
                put(JSONObject().put("id", it.id).put("name", it.name).put("contact", it.contact).put("notes", it.notes))
            }
        })

        payload.put("categories", JSONArray(data.categories))

        payload.put("budgets", JSONArray().apply {
            data.budgets.forEach { put(JSONObject().put("category", it.category).put("amount", it.amount)) }
        })

        payload.put("purchases", JSONArray().apply {
            data.purchases.forEach { p ->
                put(
                    JSONObject().apply {
                        put("id", p.id); put("item", p.item); put("category", p.category); put("sup", p.supplierId)
                        put("qty", p.qty); put("unit", p.unit); put("price", p.price); put("date", p.date)
                        put("status", p.status); put("notes", p.notes)
                        put("basis", p.basis ?: JSONObject.NULL)
                        put("groupId", p.groupId ?: JSONObject.NULL)
                        put("docs", JSONArray().apply {
                            p.docs.forEach { d -> put(JSONObject().put("name", d.name).put("type", d.type).put("date", d.date)) }
                        })
                    },
                )
            }
        })

        payload.put("groups", JSONArray().apply {
            data.groups.forEach { g ->
                put(
                    JSONObject().apply {
                        put("id", g.id); put("title", g.title); put("qty", g.qty); put("unit", g.unit)
                        put("category", g.category); put("status", g.status)
                        put("selected", g.selectedQuoteId ?: JSONObject.NULL)
                        put("purchaseId", g.purchaseId ?: JSONObject.NULL)
                        put("quotes", JSONArray().apply {
                            g.quotes.forEach { q ->
                                put(JSONObject().put("id", q.id).put("sup", q.supplierId).put("price", q.price).put("valid", q.validUntil).put("status", q.status))
                            }
                        })
                    },
                )
            }
        })

        payload.put("reminders", JSONArray().apply {
            data.reminders.forEach { r ->
                put(
                    JSONObject().apply {
                        put("id", r.id); put("title", r.title)
                        put("link", JSONObject().put("t", r.linkType).put("id", r.linkId))
                        put("due", r.due); put("status", r.status)
                    },
                )
            }
        })

        root.put("data", payload)
        return root.toString(2)
    }
}
