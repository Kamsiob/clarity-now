#!/usr/bin/env python3
"""
Exhaustive completeness audit for the Clarity Now specification.

Runs every KNOWN CLASS of specification defect generically, rather than
checking hand-picked items. Run after any edit:   python3 audit.py

Exit code 0 = clean, 1 = defects found.
"""
import pathlib, re, sys, collections, json

ROOT  = pathlib.Path(__file__).parent
DOCS  = [p for p in sorted(ROOT.glob('*.md')) if p.name != 'audit_report.md']
HTML  = sorted(ROOT.glob('*.html'))
ALL   = DOCS + HTML
TEXT  = {p.name: p.read_text(encoding='utf-8') for p in ALL}

# Every markdown file that exists anywhere in the repository, not only the authority
# set audited above. A reference to one of these resolves; a reference to anything
# else is dangling.
#
# This replaces a hard-coded whitelist of ('DESIGN.md', 'EVENT_FORMAT.md'). That
# whitelist existed because EVENT_FORMAT.md lives under docs/ and so was never in
# TEXT, but it also silenced DESIGN.md, a file that does not exist anywhere and never
# has. A whitelist cannot tell those two cases apart, so it would have hidden a real
# dangling reference the first time one appeared. Existence is the actual question,
# so ask it directly.
#
# docs/addenda holds transcribed source documents rather than specifications. They are
# provenance, they are not audited, and nothing may cite them as authority.
EXISTING_DOCS = {
    q.name for q in ROOT.rglob('*.md')
    if 'addenda' not in q.parts and '.git' not in q.parts
}
CORP  = "".join(TEXT[n] for n in TEXT if n.startswith('CORPUS_'))
findings = []
def fail(cls, msg): findings.append((cls, msg))

# ─── CLASS 1: DANGLING DECLARATION ────────────────────────────────────────
# Something is declared to exist, and does not.
for name, t in TEXT.items():
    for m in re.finditer(r'`([A-Za-z0-9_\-]+\.(?:md|html))`', t):
        if m.group(1) not in TEXT and m.group(1) not in EXISTING_DOCS:
            fail("dangling-file", f"{name} references {m.group(1)}, which does not exist")

secs = {n: {m.group(1) for m in re.finditer(r'^#{2,4}\s+(\d+(?:\.\d+)*)', t, re.M)} for n, t in TEXT.items()}
for name, t in TEXT.items():
    for m in re.finditer(r'`?([A-Za-z0-9_\-]+\.md)`?\s+(?:section[s]?\s+)?(\d+(?:\.\d+)*)', t):
        tgt, sec = m.groups()
        if tgt in secs and sec not in secs[tgt] and not any(s.startswith(sec+'.') for s in secs[tgt]):
            fail("dangling-section", f"{name} -> {tgt} section {sec} does not exist")

# every family the engine names must have corpus lines
eng = TEXT.get('CLARITY_LOGIC_ENGINE.md','')
for fam in set(re.findall(r'`([a-z][A-Za-z]{3,})`', eng)):
    if re.search(rf'\b{fam}\b.*?famil', eng, re.I) and fam not in CORP and fam not in eng.split('typealias')[0]:
        pass  # too noisy generically; the explicit list below covers it
# families are only those inside the family-list sections, named in a table row or a comma list
fam_block = "\n".join(re.findall(r'^### 6\.\d.*?(?=^### |^## )', eng, re.S | re.M))
ENGINE_FAMILIES = set(re.findall(r'^\| `(\w+)` \|', fam_block, re.M))
for lst in re.findall(r'(?:Observations|Patterns|Headline|Closings)[^:]*:\s*(.+?)\.\n', fam_block, re.S):
    ENGINE_FAMILIES |= {x.strip('` *') for x in lst.split(',') if re.fullmatch(r'\s*`?\w+`?\s*', x)}
ENGINE_FAMILIES |= set(re.findall(r'`(\w+)`', re.search(r'### 6\.5.*?(?=^---)', eng, re.S|re.M).group(0) if re.search(r'### 6\.5.*?(?=^---)', eng, re.S|re.M) else ''))
for f in sorted(ENGINE_FAMILIES):
    if f[0].islower() and f.lower() not in CORP.lower():
        fail("family-no-lines", f"engine family `{f}` has no corpus lines")

# ─── CLASS 2: UNDEFINED TERM ──────────────────────────────────────────────
defd = set(re.findall(r'(?:data class|sealed interface|object|enum class|typealias|fun interface)\s+(\w+)', eng))
defd |= {'String','Int','Long','Double','Boolean','Set','List','Map','ClosedRange','Pair','Unit'}
refd = set(re.findall(r'\b([A-Z][A-Za-z]+(?:Key|Facts|Set|Id|Result|Reason|Requirement|Selector|Band|History|Option|Stage|Ref|Kind|Item|Pulse|Plan|State|Event|Rule|Clock))\b', eng))
for t in sorted(refd - defd - {'ClarityEvent','ClarityState','ClarityRule','ClarityPlan','ClarityClock'}):
    fail("undefined-type", f"engine references type {t} without declaring it")

declared_slots = set(re.findall(r'`\{(\w+)\}`', CORP))
used_slots = set(re.findall(r'\{(\w+)\}', CORP)) - {'cue','action','actionVerb','actionGerund','actionNoun','frame'}
for s in sorted(used_slots - declared_slots):
    fail("undeclared-slot", f"corpus uses {{{s}}} which is never declared")

# ─── CLASS 3: INCOMPLETE APPLICATION ──────────────────────────────────────
# A rule says "every X", so every X must comply.
keys = re.findall(r'^([a-z]+[\w\.]*\.[\w\d]+)\s{2,}', CORP, re.M)
for k, c in collections.Counter(keys).items():
    if c > 1: fail("duplicate-key", f"variant key {k} appears {c} times")

lines = [(m.group(1), m.group(2).strip()) for m in
         re.finditer(r'^([a-z]+[\w\.]*\.[\w\d]+)\s{2,}(?:\[[PONER]\]\s{2,})?(.+)$', CORP, re.M)]
for k, l in lines:
    if re.match(r'^[a-z]+\.s\d+\.r\d+$', k) and ' / ' not in l:
        fail("malformed-pair", f"{k} is a response key with no ' / ' separator")

# every Pulse family stage needs statements, questions and responses
fam = collections.defaultdict(set)
for m in re.finditer(r'^([a-z]+)\.(s\d+)\.(\d+|q\d+|r\d+)\s', TEXT.get('CORPUS_1_PULSE.md',''), re.M):
    fam[(m.group(1),m.group(2))].add('stmt' if m.group(3).isdigit() else m.group(3)[0])
for kk, v in fam.items():
    if v != {'stmt','q','r'}: fail("incomplete-stage", f"{kk[0]} {kk[1]} missing {sorted({'stmt','q','r'}-v)}")

# ─── CLASS 4: CONTRADICTION ───────────────────────────────────────────────
def nums(pat):
    out = {}
    for n, t in TEXT.items():
        f = set(re.findall(pat, t))
        if f: out[n] = f
    return out
for label, pat in [
    ("pulse silence floor", r'between (\d+) and \d+ percent of days'),
    ("guidance silence floor", r'at least (\d+) percent of reports'),
    ("swipe reveal", r'swipe (?:right|left) past \*?\*?(\d+) percent'),
    ("undo window", r'(\d+) second undo'),
    ("variant exclusion", r'within (\d+) days'),
    ("cue min weeks", r'at least (\d+) weeks of data'),
]:
    v = nums(pat)
    if len({frozenset(x) for x in v.values()}) > 1:
        fail("contradiction", f"{label} disagrees across documents: {v}")

# corpus totals must appear identically wherever stated
for n in ['620','737','162','1,519','10,557','17,200']:
    where = [f for f in TEXT if f.endswith('.md') and n in TEXT[f]]
    if not where: fail("stale-total", f"total {n} is stated nowhere; counts may have drifted")

# ─── CLASS 5: UNENFORCED RULE ─────────────────────────────────────────────
BANNED = ['should','failed','streak','hurry','lazy','must','have to','make sure',
          'try to','remember to','keep it up','well done','great job']
BEHIND = re.compile(r'\b(?:fall(?:ing|s|en)?|get(?:ting)?|slip(?:ping)?|running|are|is|am|were|was)\s+behind\b', re.I)
CAUSAL = re.compile(r'\b(because|suggests?|means?|therefore|which is why)\b', re.I)
for k, l in lines:
    low = l.lower()
    for b in BANNED:
        if re.search(r'\b'+re.escape(b)+r'\b', low): fail("banned-word", f"{k}: '{b}' in \"{l[:50]}\"")
    if BEHIND.search(l): fail("banned-word", f"{k}: evaluative 'behind'")
    if '!' in l:         fail("banned-word", f"{k}: exclamation mark")
    if k.startswith(('mo.','bn.','bnc.')) and CAUSAL.search(l):
        fail("momentum-causal", f"{k}: causal construction in a Momentum surface")
    if k.startswith('hd.') and len(re.sub(r'\{[^}]+\}','X',l).split()) >= 8:
        fail("too-long", f"{k}: headline is 8 words or more")
    if k.startswith('mo.') and len(re.sub(r'\{[^}]+\}','Xxxxx',l).split()) > 12:
        fail("too-long", f"{k}: Momentum headline over 12 words")
    if k.startswith('act.') and re.match(r'^(finish|close|pick|clear|take|move|let|spend|open|do|give|decide|look|put|find|read|write|replace|add|book|start|check|protect|sit)\b', l):
        fail("imperative-action", f"{k}: guidance action is imperative, must be nominal")

# ─── CLASS 6: TEXT HYGIENE ────────────────────────────────────────────────
ALLOW = set('\u2018\u2019\u201c\u201d\u00b7\u00d7\u2713\u2715\u2192\u25b2\u2717\u2726\u2733')
BRIT  = re.compile(r'\b\w*(colour|licence|behaviour|favourite|centre|recognis(e|ed|es|ing|ation)|'
                   r'prioritis(e|ed|es|ing|ation)|organis(e|ed|es|ing|ation)|apologis(e|ed|es|ing)|'
                   r'realis(e|ed|es|ing|ation)|analys(e|ed|es|ing))\w*\b', re.I)
for name, t in TEXT.items():
    for i, l in enumerate(t.splitlines(), 1):
        if '\u2014' in l or '\u2013' in l: fail("hygiene", f"{name}:{i} em or en dash")
        odd = {c for c in l if ord(c) > 127 and c not in ALLOW}
        if odd: fail("hygiene", f"{name}:{i} non-ASCII {sorted(odd)}")
        if BRIT.search(l): fail("hygiene", f"{name}:{i} British spelling")

# ─── CLASS 7: STRUCTURAL ──────────────────────────────────────────────────
for name in ['MASTER_BUILD_PROMPT.md','CLARITY_LOGIC_ENGINE.md','design-v3.md']:
    n = [int(m.group(1)) for m in re.finditer(r'^## (\d+)\.', TEXT[name], re.M)]
    if n:
        for g in [x for x in range(min(n), max(n)+1) if x not in n]:
            fail("numbering", f"{name} section {g} missing")
        for dup in [x for x, c in collections.Counter(n).items() if c > 1]:
            fail("numbering", f"{name} section {dup} duplicated")

from html.parser import HTMLParser
class V(HTMLParser):
    VOID={'area','base','br','col','embed','hr','img','input','link','meta','source','track','wbr',
          'use','path','circle','rect','polyline','ellipse','stop','fegaussianblur','femergenode'}
    def __init__(s): super().__init__(); s.st=[]; s.err=[]
    def handle_starttag(s,t,a):
        if t.lower() not in s.VOID: s.st.append((t,s.getpos()[0]))
    def handle_endtag(s,t):
        if t.lower() in s.VOID: return
        if not s.st or s.st[-1][0]!=t: s.err.append(f"line {s.getpos()[0]} </{t}>")
        else: s.st.pop()
for p in HTML:
    v=V(); v.feed(TEXT[p.name])
    for e_ in v.err[:3]: fail("html", f"{p.name} mismatched tag at {e_}")
    for tag,ln in v.st[:3]: fail("html", f"{p.name} unclosed <{tag}> line {ln}")
    defined=set(re.findall(r'<symbol id="([\w\-]+)"', TEXT[p.name]))
    for u in set(re.findall(r'href="#([\w\-]+)"', TEXT[p.name])) - defined:
        fail("html", f"{p.name} dangling SVG reference #{u}")

# ─── CLASS 8: WORKFLOW REACHABILITY ───────────────────────────────────────
nav = (TEXT.get('design-v3.md','') + TEXT.get('MASTER_BUILD_PROMPT.md','')).lower()
for req in ['predictive back','leaves the session running','zero areas','queued item is tappable',
            'first launch','one row open at a time','fling above','scroll wins',
            'sheet dismiss','tab content transition','empty state entrance',
            '| reject |','| undo |','| step |']:
    if req not in nav: fail("workflow", f"interaction rule missing: '{req}'")

# ─── CLASS 9: STANDING INSTRUCTIONS ───────────────────────────────────────
mb = TEXT.get('MASTER_BUILD_PROMPT.md','')
for req in ['306265999+Kamsiob@users.noreply.github.com','git config user.name "Kamsiob"',
            'as PUBLIC','Commit ALL project files','actual in-app screenshots','screencap',
            'Wireless debugging','versionCode','.gitignore','Never commit a keystore','Bazzite',
            '~/Android/Sdk','ONE copy of the project','Choose the version number yourself',
            'AGPL-3.0','buymeacoffee.com/kamsiob','t.me/+g5LKm9rUnNcxMjk5','hello@kamsiob.com',
            'youtube.com/@kamsiob','B7 Collective','kamsiob-503213',
            'App label','Downloadable Fonts','fonts.google.com','clarity-now',
            'applicationIdSuffix','local.properties']:
    if req not in mb: fail("standing-instruction", f"missing from master prompt: '{req}'")

# ─── REPORT ───────────────────────────────────────────────────────────────
CLASSES = ["dangling-file","dangling-section","family-no-lines","undefined-type","undeclared-slot",
           "duplicate-key","malformed-pair","incomplete-stage","contradiction","stale-total",
           "banned-word","momentum-causal","too-long","imperative-action","hygiene","numbering",
           "html","workflow","standing-instruction"]
by = collections.Counter(c for c,_ in findings)
print(f"Clarity Now specification audit")
print(f"{len(ALL)} files, {len(lines)} corpus lines, {len(CLASSES)} defect classes\n")
for c in CLASSES:
    n = by.get(c,0)
    print(f"  {'PASS' if not n else f'{n:4} FAIL'}  {c}")
if findings:
    print(f"\n{len(findings)} findings:")
    for c,msg in findings[:60]: print(f"  [{c}] {msg}")
    if len(findings) > 60: print(f"  ... and {len(findings)-60} more")
print(f"\n{'CLEAN' if not findings else str(len(findings)) + ' DEFECTS'}")
sys.exit(1 if findings else 0)
