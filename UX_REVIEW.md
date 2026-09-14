# Guess Market — UX / Design Review

Two passes over the actual current `main.fxml` + `style.css` (Classic theme), Events and Users tabs. Since this isn't a SaaS landing page, "make every visitor start a free trial" is read as its real equivalent here: **get a first-time user to load a file and complete their first trade without hesitating, backtracking, or wondering if something's broken.** Findings sorted Critical → High Impact → Nice-to-Have within each pass.

---

## Pass 1 — The Designer

*Ripping apart hierarchy, visual decisions, and activation leaks.*

### 🔴 Critical

**1. The primary action is 3–4 scrolls below the fold.**
Select a row in the events table and the right-hand panel stacks, in order: eyebrow label → heading → status pills → "Current State" → two option cards → an (empty) Price History chart → Order Book tables (if applicable) → Event Participants table → *finally* the Trade/Manage Event card → Trade History. The one thing you actually want someone to do — place a trade — is the **8th section down** in a single unbroken vertical scroll. No SaaS product with a real activation metric would bury its primary CTA this deep. This isn't a layout bug, it's a hierarchy failure: everything is weighted equally, so nothing tells the eye where to go.

**2. Empty states render as broken states.**
Price History is a full 180px `LineChart` with axis labels and *zero data* the moment you select any event that hasn't traded yet. Order Book tables, Event Participants, and Trade History all render as empty-but-fully-chromed tables (header row, borders, whitespace) before any activity exists. A first click produces a screen that looks unfinished or malfunctioning, not "ready and waiting." Every one of these needs a collapsed/placeholder state ("No trades yet") instead of an empty grid — the app already does this correctly for `orderBookContainer` (hidden for LMSR events); the same discipline needs to extend to the chart and the two other tables.

**3. The balance badge is always green, including when it shouldn't be.**
`lblEventBalance` and `lblUserBalance` are hardcoded to `stat-badge-positive` (green) regardless of the actual number. A blocked user with a negative balance still sees their balance rendered as a "success" pill. This isn't a style nitpick — it's a semantic color lie, and it's exactly the kind of thing that makes an app feel unpolished the moment someone hits it (and someone *will* hit it, since going negative and getting blocked is a designed, expected flow in this system, not an edge case).

### 🟠 High Impact

**4. One fact, three redundant representations, zero visual grouping.**
"What's this option worth right now" is answered independently by: the option-card text block, the Price History chart, and (for Order Book) the pending-orders tables — three unrelated-looking sections with no shared container, tab, or visual thread connecting them. A user has to mentally reconcile three separate widgets instead of reading one clear answer.

**5. The header toolbar has no priority order.**
Title, theme dropdown, animations checkbox, file path field, Load File button, progress bar — six controls, one row, identical visual weight. "Load File" is the only thing a brand-new user needs to find, and it's competing on equal footing with a theme switcher. A first-run header should make exactly one thing obvious.

**6. Tiles view doesn't earn its existence.**
The Tiles toggle promises a different, presumably more scannable way to browse events. What it delivers is the same five data points as the table, restacked as plain `Label`s inside a bordered box — no icon, no visual differentiation by status, no size hierarchy. It's the table's data in a worse-for-scanning shape.

**7. Trade form fields lose their labels the moment you use them.**
`txtOrderPrice` / `txtSharesAmount` have no persistent field labels — only prompt text ("Price", "Qty") that vanishes on focus. Anyone who tabs away and back, or is reviewing a half-filled form, is looking at two unlabeled number fields next to an "Order Type" dropdown.

### 🟢 Nice-to-Have

**8. Badge overload dilutes the one number that matters.** Filter pills, view-toggle pills, and stat badges all use the same rounded-pill visual language for three unrelated jobs (filtering, navigation, data display). Nothing is allowed to be *the* important number — balance should visually outrank "Commission Collected" and "Winner," not sit at equal size next to them.

**9. The Create Event dialog is a plain vertical stack of ~10 fields** built ad hoc in Java rather than sharing the app's responsive layout patterns. Grouping into Basics / Pricing / Type-specific (even just with section labels) would cut the perceived form length in half.

---

## Pass 2 — First-Time User, Clicking Through

*Where I got confused, felt nagged, or wanted to leave.*

### 🔴 Critical

**1. "I clicked a row and nothing happened."**
The event detail pane lives in the same `ScrollPane` whose scroll position doesn't reset on selection. If I'd scrolled down browsing a previous event, clicking a new row in the table updates content that's now sitting *above my current scroll position*, off-screen. From my seat, I clicked something and the screen didn't change. I'd click it again, maybe twice, before scrolling up to investigate.

**2. "Which one of these am I?"**
There's no login. To trade, I pick myself from a `cbActiveUser` dropdown. As a first-time user this reads as: *is this a shared computer? Am I supposed to know which name is mine? Can I just click anyone's?* Nothing in the UI explains the "you are whichever user you select" model — it's a natural artifact of a no-auth course exercise, but to a fresh user it looks like a missing login screen.

**3. "Did my trade actually do anything?"**
After clicking Execute Trade, I get a blocking native alert with numbers, which I dismiss — and then I'm still looking at the trade form at the bottom of the page. The balance that changed is back at the top, out of view. The only feedback *in place* is a 160ms pulse on the card I'm already looking at, which doesn't tell me *what* changed, just that *something* did. I have to scroll up on faith to confirm the trade worked.

### 🟠 High Impact

**4. "Why did the Open/Close buttons just disappear?"**
If I'm not the event's Market Maker, `btnOpenEvent` and the entire `closeEventBox` aren't greyed out — they're gone, with zero explanation. As a regular participant I have no way to discover these actions exist at all, let alone understand why I can't use them. A disabled button with a tooltip ("Only the Market Maker can open this event") teaches me something; a vanished button teaches me nothing and makes the app feel like it's hiding functionality.

**5. "What's LMSR? What's Order Book? Why does this event's price section look different from that one?"**
The Type filter exposes raw internal terminology (LMSR / Order Book) with no explanation anywhere in the UI, and the option-card content genuinely changes shape between event types (Price/Shares vs. LAST/BID/ASK/MID/SPREAD) with no framing for why. I'm left pattern-matching instead of being told.

**6. "I created an event — now what?"**
After the Create Event dialog succeeds, I get an alert saying I'm now the Market Maker and can open it "from the event's Trade/Manage panel" — but I'm not taken there. I have to go find my own new event in the table myself, re-select it, scroll all the way down, and only then discover the Open Event button.

**7. Nested scrolling on every table.**
Every table sits inside the page's own scroll area but has its own internal scrollbar once rows exceed ~4–5. Scrolling the page while my cursor happens to be over a table scrolls *the table*, not the page — I have to consciously move my mouse to the margin to keep reading downward. This happens repeatedly on a page this long.

### 🟢 Nice-to-Have

**8. Theme and Animations controls, front and center, before I've done anything.** Fun for a returning user, pure noise for a first-time one still looking for Load File.

**9. No inline validation.** Typing letters into the Qty field, or a price above the allowed max, only surfaces as an error *after* I click Execute — a live inline hint would catch it before I commit.

---

## If I could only fix three things

1. Collapse empty sections (chart, Order Book, Participants, Trade History) instead of rendering them chromed-but-blank.
2. Pin or float the Trade/Manage Event card so it's reachable without scrolling past everything else — or move it directly under "Current State."
3. Fix the balance badge's color logic so it actually reflects positive vs. blocked, not a hardcoded green.
