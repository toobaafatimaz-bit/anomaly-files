package com.example.data.cases

import com.example.model.*
import kotlin.random.Random

object CaseCatalogPart2 {

    // CASE 005: THE SILENT HOSPITAL
    private val case005 = CaseDefinition(
        id = "case_005",
        caseNumber = "CASE #005",
        title = "THE SILENT HOSPITAL",
        locationName = "Mercy General — Quarantine Ward 4",
        incidentSummary = "Ward 4 was shuttered in 2012, yet pharmacy logs show daily prescriptions dispensed to forty-two active patients. No living patients exist inside—only fresh IV bags dripping into empty beds.",
        witnessCount = 2,
        suspiciousActivities = listOf(
            "Heart monitors flatline yet chart steady pulses",
            "Medical charts list Investigator 049 as admitted in 2019",
            "Surgical lights snap on whenever your back is turned"
        ),
        threatClassification = ThreatLevel.CLASS_IV,
        primaryObjective = "Locate the forty-two registered patients and identify why your own file appears in Ward 4.",
        optionalSecretObjective = "Inspect under Bed 04-B in the ICU and scan the operating mirror.",
        wallTheme = WallTheme.HOSPITAL,
        mapGrid = listOf(
            "############",
            "#P..#......#",
            "#...D......#",
            "###D####D###",
            "#.....#....#",
            "#.....D....#",
            "#..S..#....#",
            "#.....#....#",
            "#..M..#....#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c5_d1", name = "Ward Airlock", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 2.5f, promptText = "Open Ward Airlock",
                baseDescription = "Pressure seal hisses softly.", isDoor = true, doorGridX = 4, doorGridY = 2
            ),
            InteractiveObject(
                id = "c5_d2", name = "ICU Wing Door", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f, promptText = "Open ICU Wing",
                baseDescription = "Double swing doors with yellowed glass.", isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c5_d3", name = "Records Vault Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Open Records Vault",
                baseDescription = "Keypad bypassed cleanly.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c5_d4", name = "Operating Theatre Door", kind = ObjectKind.DOOR,
                gridX = 6.5f, gridY = 5.5f, promptText = "Open Operating Theatre",
                baseDescription = "Smells faintly of ozone and antiseptic.", isDoor = true, doorGridX = 6, doorGridY = 5
            ),
            InteractiveObject(
                id = "c5_d5", name = "Isolation Room 04", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 6.5f, promptText = "Open Isolation 04",
                baseDescription = "Steel door scratched from the inside.", isDoor = true, doorGridX = 3, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c5_chart", name = "Patient Intake Computer", kind = ObjectKind.COMPUTER,
                gridX = 8.5f, gridY = 1.5f, promptText = "Check Medical Records",
                baseDescription = "42 patients listed as 'CONSCIOUSNESS ANCHORED'. Entry #42 bears your name and blood type.",
                linkedEvidenceId = "c5_ev_records"
            ),
            InteractiveObject(
                id = "c5_monitor", name = "ICU Heartbeat Monitor", kind = ObjectKind.TV_SCREEN,
                gridX = 2.5f, gridY = 4.5f, promptText = "Analyze Monitor",
                baseDescription = "Screen shows 72 BPM synced to your own pulse, though the leads hang disconnected.",
                audioSnippet = "Audio Analyzer catches a doctor's voice: 'Subject 049 memory graft stable.'",
                emfStrength = 0.8f,
                linkedEvidenceId = "c5_ev_monitor"
            ),
            InteractiveObject(
                id = "c5_mirror", name = "Isolation Observation Glass", kind = ObjectKind.MIRROR,
                gridX = 3.5f, gridY = 8.5f, promptText = "Inspect Observation Glass",
                baseDescription = "In the reflection, you are wearing a hospital gown instead of your DUI coat.",
                spectralDescription = "UV markings: 'MEMORY WIPE #07 COMPLETE'.",
                linkedEvidenceId = "c5_ev_glass"
            ),
            InteractiveObject(
                id = "c5_bed_clue", name = "Bed 04-B Discharge Tag", kind = ObjectKind.FLOOR_CLUE,
                gridX = 9.5f, gridY = 7.5f, requiresCrouch = true,
                promptText = "Retrieve Bed Tag (Crouched)",
                baseDescription = "Signed by the DUI Director: 'Transfer Agent 049 back to Briefing Room prior to waking.'",
                linkedEvidenceId = "c5_ev_secret_transfer"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c5_ev_records", title = "Ward 4 Intake Registry",
                category = EvidenceCategory.DIGITAL,
                summary = "42 patients listed as 'Consciousness Anchored'; includes Agent 049.",
                detailedAnalysis = "Dates correspond to DUI field casualties.",
                anomalyCategory = AnomalyCategory.BIOLOGICAL
            ),
            EvidenceDefinition(
                id = "c5_ev_monitor", title = "Synced Biometric Telemetry",
                category = EvidenceCategory.AUDIO,
                summary = "Disconnected monitor mirrors your pulse; EVP mentions 'memory graft'.",
                detailedAnalysis = "Sub-audible loop broadcasts physiological telemetry.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c5_ev_glass", title = "Patient Reflection Anomaly",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Observation glass reflects you in patient attire with wipe #07 markings.",
                detailedAnalysis = "Memory-linked perceptual distortion.",
                anomalyCategory = AnomalyCategory.VISUAL
            ),
            EvidenceDefinition(
                id = "c5_ev_secret_transfer", title = "CLASSIFIED: Director's Transfer Order",
                category = EvidenceCategory.PHYSICAL,
                summary = "Order authorizing your transfer from Mercy ICU to the dark DUI Briefing Room.",
                detailedAnalysis = "Explains why you woke up in the dark room with no memory at the start of Case #001.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_silent_05", type = EntityType.THE_SILENT,
                startX = 8.5f, startY = 5.5f,
                patrolPoints = listOf(8.5f to 5.5f, 8.5f to 8.5f, 7.5f to 8.5f, 7.5f to 4.5f),
                baseHeat = 0.1f
            ),
            SceneEntity(
                id = "ent_rep_05", type = EntityType.THE_REPLACEMENT,
                startX = 4.5f, startY = 5.5f,
                patrolPoints = listOf(1.5f to 5.5f, 4.5f to 5.5f),
                baseHeat = 0.2f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c5_main",
                requiredEvidenceIds = setOf("c5_ev_records", "c5_ev_monitor"),
                title = "Phantom Patient Containment",
                conclusion = "Ward 4 hosts residual consciousness anchors monitored by The Silent.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c5_secret",
                requiredEvidenceIds = setOf("c5_ev_secret_transfer", "c5_ev_glass"),
                title = "Agent 049 Memory Reset",
                conclusion = "You were a patient in Ward 4 before DUI wiped your memory and returned you to duty.",
                isValidDeduction = true, unlocksSecretClue = true
            )
        ),
        correctClassification = AnomalyClassification.UNCLASSIFIED_PHENOMENON,
        correctThreatLevel = ThreatLevel.CLASS_IV,
        correctAction = RecommendedAction.INTERNAL_AUDIT,
        conspiracyRevealText = "Your wrist still carries the faint circular scar of an IV shunt.",
        unlockRequirementCaseId = "case_004"
    )

    // CASE 006: THE ABANDONED LABORATORY
    private val case006 = CaseDefinition(
        id = "case_006",
        caseNumber = "CASE #006",
        title = "THE ABANDONED LABORATORY",
        locationName = "DUI Sub-Level Lab 07 (Decommissioned)",
        incidentSummary = "All twelve scientists inside Sub-Level 07 vanished mid-sentence during Project Mirror-0. Security doors unlocked from inside six years later.",
        witnessCount = 0,
        suspiciousActivities = listOf(
            "Containment glass fractured from the observer side",
            "Server core still compiling behavioral models of you",
            "The Observer appears directly behind containment barriers"
        ),
        threatClassification = ThreatLevel.CLASS_V,
        primaryObjective = "Uncover what Project Mirror-0 created inside Sub-Level 07.",
        optionalSecretObjective = "Reconstruct the scientists' final moments in the Containment Chamber with Memory Recorder.",
        wallTheme = WallTheme.LABORATORY,
        mapGrid = listOf(
            "############",
            "#P..#......#",
            "#...D......#",
            "###D####D###",
            "#..........#",
            "#..######..#",
            "#..S....D..#",
            "#..#....#..#",
            "#..#..M.#..#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c6_d1", name = "Decontamination Airlock", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 2.5f, promptText = "Cycle Airlock",
                baseDescription = "Cyan decontamination lights flicker.", isDoor = true, doorGridX = 4, doorGridY = 2
            ),
            InteractiveObject(
                id = "c6_d2", name = "Lab Floor Gate", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f, promptText = "Open Lab Floor",
                baseDescription = "Blast door half-ajar.", isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c6_d3", name = "Server Core Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Open Server Core",
                baseDescription = "Cooling fans still hum quietly.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c6_d4", name = "Containment Breach Door", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 6.5f, promptText = "Enter Mirror-0 Chamber",
                baseDescription = "Warning tape: 'PROJECT MIRROR-0 — DO NOT OBSERVE'.", isDoor = true, doorGridX = 3, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c6_d5", name = "Observation Deck Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 6.5f, promptText = "Open Observation Deck",
                baseDescription = "Reinforced glass frame.", isDoor = true, doorGridX = 8, doorGridY = 6
            ),
            InteractiveObject(
                id = "c6_server", name = "Project Mirror-0 Mainframe", kind = ObjectKind.COMPUTER,
                gridX = 9.5f, gridY = 1.5f, promptText = "Access Mainframe",
                baseDescription = "Logs show Intelligent Anomalies weren't captured—they were trained inside simulation loops modeled on DUI investigators.",
                emfStrength = 0.92f,
                linkedEvidenceId = "c6_ev_mainframe"
            ),
            InteractiveObject(
                id = "c6_chamber", name = "Shattered Containment Tank", kind = ObjectKind.TV_SCREEN,
                gridX = 5.5f, gridY = 7.5f, promptText = "Scan Containment Tank",
                baseDescription = "Empty acrylic cylinder. Inside sits an exact replica of your own investigator flashlight.",
                memoryEchoDescription = "Echo: Lead scientist Dr. Kael yells: 'It isn't mimicking us anymore—it wrote its own case file!'",
                realityDistortion = 0.9f,
                linkedEvidenceId = "c6_ev_echo"
            ),
            InteractiveObject(
                id = "c6_mirror_0", name = "Mirror-0 Calibration Glass", kind = ObjectKind.MIRROR,
                gridX = 6.5f, gridY = 8.5f, promptText = "Inspect Calibration Mirror",
                baseDescription = "Your reflection raises its hand and points toward a floor grate.",
                linkedEvidenceId = "c6_ev_mirror"
            ),
            InteractiveObject(
                id = "c6_grate", name = "Floor Grate Cipher", kind = ObjectKind.FLOOR_CLUE,
                gridX = 1.5f, gridY = 7.5f, requiresCrouch = true,
                promptText = "Inspect Floor Grate (Crouched)",
                baseDescription = "Engraved in steel: 'ORIGIN FILE: 049 WAS NEVER HUMAN. BUILT TO FIND US.'",
                linkedEvidenceId = "c6_ev_secret_origin"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c6_ev_mainframe", title = "Mirror-0 Training Logs",
                category = EvidenceCategory.DIGITAL,
                summary = "DUI deliberately cultivated Intelligent Anomalies using investigator behavior.",
                detailedAnalysis = "Explains why anomalies adapt dynamically to thermal/mirror/camera habits.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c6_ev_echo", title = "Dr. Kael's Final Echo",
                category = EvidenceCategory.AUDIO,
                summary = "Containment subject escaped and began authoring its own investigation files.",
                detailedAnalysis = "Voice stress analysis shows extreme terror prior to blackout.",
                anomalyCategory = AnomalyCategory.TEMPORAL
            ),
            EvidenceDefinition(
                id = "c6_ev_mirror", title = "Autonomous Calibration Reflection",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Reflection acts independently to guide investigator.",
                detailedAnalysis = "Matches behavior of THE OBSERVER.",
                anomalyCategory = AnomalyCategory.VISUAL
            ),
            EvidenceDefinition(
                id = "c6_ev_secret_origin", title = "CLASSIFIED: Origin Cipher 049",
                category = EvidenceCategory.PHYSICAL,
                summary = "'049 WAS NEVER HUMAN. BUILT TO FIND US.'",
                detailedAnalysis = "Confirms you share the same origin as the Fifth Resident.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_obs_06", type = EntityType.THE_OBSERVER,
                startX = 7.5f, startY = 4.5f,
                patrolPoints = listOf(1.5f to 4.5f, 9.5f to 4.5f),
                disappearsOnApproach = true,
                baseHeat = 0.25f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c6_main",
                requiredEvidenceIds = setOf("c6_ev_mainframe", "c6_ev_echo"),
                title = "Engineered Intelligent Anomalies",
                conclusion = "DUI created the Intelligent Anomalies in Lab 07 before losing containment.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c6_secret",
                requiredEvidenceIds = setOf("c6_ev_secret_origin", "c6_ev_mirror"),
                title = "Project Mirror-0 Synthesis",
                conclusion = "Agent 049 was synthesized inside Mirror-0 to hunt escaped anomalies.",
                isValidDeduction = true, unlocksSecretClue = true
            )
        ),
        correctClassification = AnomalyClassification.UNCLASSIFIED_PHENOMENON,
        correctThreatLevel = ThreatLevel.CLASS_V,
        correctAction = RecommendedAction.INTERNAL_AUDIT,
        conspiracyRevealText = "Every alarm in Sector 04 briefly switches from amber to spectral violet upon your return.",
        unlockRequirementCaseId = "case_005"
    )

    // CASE 007: THE MIRROR HOUSE
    private val case007 = CaseDefinition(
        id = "case_007",
        caseNumber = "CASE #007",
        title = "THE MIRROR HOUSE",
        locationName = "Vance Glass Estate — Hall of Reflections",
        incidentSummary = "Every reflective surface in the Vance Estate depicts an alternate version of the room—doors appear in mirrors that do not exist behind you, and visitors walk into reflections and vanish.",
        witnessCount = 1,
        suspiciousActivities = listOf(
            "Mirrors reflect open doors where walls stand",
            "Thermals invert inside the Hall of Mirrors",
            "Multiple Copies patrol in symmetric patterns"
        ),
        threatClassification = ThreatLevel.CLASS_IV,
        primaryObjective = "Identify which reflection anchors the real world and locate the missing owner.",
        optionalSecretObjective = "Check the cracked attic mirror while crouched to uncover the Fifth Resident's true name.",
        wallTheme = WallTheme.MIRROR_HOUSE,
        mapGrid = listOf(
            "############",
            "#P.M.#..M..#",
            "#....D.....#",
            "###D####D###",
            "#..M....M..#",
            "#..........#",
            "####S#######",
            "#..........#",
            "#..M....M..#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c7_d1", name = "Foyer Arch", kind = ObjectKind.DOOR,
                gridX = 5.5f, gridY = 2.5f, promptText = "Open Foyer Arch",
                baseDescription = "Ornate silvered doorframe.", isDoor = true, doorGridX = 5, doorGridY = 2
            ),
            InteractiveObject(
                id = "c7_d2", name = "Hall of Mirrors West", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f, promptText = "Enter Hall of Mirrors",
                baseDescription = "Glass chimes rattle silently.", isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c7_d3", name = "Parlour East", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Enter East Parlour",
                baseDescription = "Heavy velvet curtains.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c7_d4", name = "Reflected Staircase Door", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 6.5f, promptText = "Step Through Mirror Door",
                baseDescription = "Only appears solid when viewed at an angle.", isDoor = true, doorGridX = 4, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c7_m1", name = "Foyer Tall Mirror", kind = ObjectKind.MIRROR,
                gridX = 3.5f, gridY = 1.5f, promptText = "Inspect Foyer Mirror",
                baseDescription = "Shows Apartment 4B's hallway directly behind you instead of the Vance foyer.",
                realityDistortion = 0.88f,
                linkedEvidenceId = "c7_ev_mirror_link"
            ),
            InteractiveObject(
                id = "c7_m2", name = "Parlour Infinity Glass", kind = ObjectKind.MIRROR,
                gridX = 8.5f, gridY = 4.5f, promptText = "Inspect Infinity Glass",
                baseDescription = "Seven reflections of you stand in line—the seventh turns its head to look at you.",
                spectralDescription = "Spectral Lens highlights a hidden key behind the glass.",
                linkedEvidenceId = "c7_ev_seventh"
            ),
            InteractiveObject(
                id = "c7_journal", name = "Julian Vance's Diary", kind = ObjectKind.DOCUMENT,
                gridX = 8.5f, gridY = 7.5f, promptText = "Read Estate Diary",
                baseDescription = "'The Fifth Resident isn't an intruder. It is the half of us the Department cut away to seal the breach.'",
                linkedEvidenceId = "c7_ev_diary"
            ),
            InteractiveObject(
                id = "c7_shard", name = "Cracked Attic Mirror Shard", kind = ObjectKind.FLOOR_CLUE,
                gridX = 2.5f, gridY = 8.5f, requiresCrouch = true,
                promptText = "Inspect Fallen Shard (Crouched)",
                baseDescription = "Etched on the silver backing: 'CIPHER KEY V: THE DOOR OPENS BOTH WAYS — 049 + 050'.",
                linkedEvidenceId = "c7_ev_secret_cipher"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c7_ev_mirror_link", title = "Cross-Location Mirror Bridge",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Vance mirror reflects Apartment 4B in real time.",
                detailedAnalysis = "Proves all anomaly sites share a connected spatial fold.",
                anomalyCategory = AnomalyCategory.SPATIAL
            ),
            EvidenceDefinition(
                id = "c7_ev_seventh", title = "Autonomous Seventh Reflection",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Seventh mirror iteration moves independently of the observer.",
                detailedAnalysis = "Classic signature of THE COPY achieving autonomy.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c7_ev_diary", title = "Julian Vance's Final Entry",
                category = EvidenceCategory.WITNESS,
                summary = "Claims the Fifth Resident is a severed counterpart of the investigator.",
                detailedAnalysis = "Links Blackwood Apartment 4B directly to Project Mirror-0.",
                anomalyCategory = AnomalyCategory.BIOLOGICAL
            ),
            EvidenceDefinition(
                id = "c7_ev_secret_cipher", title = "CLASSIFIED: Mirror Cipher V",
                category = EvidenceCategory.PHYSICAL,
                summary = "'THE DOOR OPENS BOTH WAYS — 049 + 050'.",
                detailedAnalysis = "You are 049. The figure in your personnel photograph is 050.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_copy_07", type = EntityType.THE_COPY,
                startX = 5.5f, startY = 4.5f,
                patrolPoints = listOf(2.5f to 5.5f, 8.5f to 5.5f),
                baseHeat = 0.65f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c7_main",
                requiredEvidenceIds = setOf("c7_ev_mirror_link", "c7_ev_seventh"),
                title = "Connected Spatial Mirror Network",
                conclusion = "Reflective surfaces across cases act as portals controlled by autonomous Copies.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c7_secret",
                requiredEvidenceIds = setOf("c7_ev_secret_cipher", "c7_ev_diary"),
                title = "Subject 050 Identification",
                conclusion = "The figure haunting your photograph is Subject 050—your severed twin from Mirror-0.",
                isValidDeduction = true, unlocksSecretClue = true
            )
        ),
        correctClassification = AnomalyClassification.INTELLIGENT_ANOMALY,
        correctThreatLevel = ThreatLevel.CLASS_IV,
        correctAction = RecommendedAction.CONTAIN,
        conspiracyRevealText = "Looking closely at your HQ Personnel Photo, Subject 050 is no longer behind your shoulder—it is standing beside you.",
        unlockRequirementCaseId = "case_006"
    )

    // CASE 008: THE LAST BROADCAST (FINALE)
    private val case008 = CaseDefinition(
        id = "case_008",
        caseNumber = "CASE #008",
        title = "THE LAST BROADCAST",
        locationName = "Channel 6 Ruin — Broadcast Tower Core",
        incidentSummary = "Channel 6 was demolished twelve years ago, yet its emergency signal hijacked every monitor inside DUI Sector 04 tonight. The broadcast shows live footage of you walking up these stairs.",
        witnessCount = 1,
        suspiciousActivities = listOf(
            "Demolished studio stands intact inside local temporal pocket",
            "Broadcast loop replays your entire career from Case #001",
            "DUI Director orders immediate permanent seal of the tower"
        ),
        threatClassification = ThreatLevel.CLASS_V,
        primaryObjective = "Reach the Broadcast Control Booth and decide the fate of the Department of Unusual Incidents.",
        optionalSecretObjective = "Decode the Master Tape beneath the anchor desk to unlock the final truth.",
        wallTheme = WallTheme.BROADCAST,
        mapGrid = listOf(
            "############",
            "#P..#......#",
            "#...D......#",
            "###D####D###",
            "#..........#",
            "#..#....#..#",
            "#..D....S..#",
            "#..#....#..#",
            "#..#..M.#..#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c8_d1", name = "Studio Lobby Door", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 2.5f, promptText = "Open Studio Door",
                baseDescription = "ON AIR sign glows emergency red.", isDoor = true, doorGridX = 4, doorGridY = 2
            ),
            InteractiveObject(
                id = "c8_d2", name = "Soundstage Gate", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f, promptText = "Enter Soundstage",
                baseDescription = "Thick soundproof door.", isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c8_d3", name = "Tape Vault Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Open Tape Vault",
                baseDescription = "Stamped 'DUI OMEGA CLEARANCE'.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c8_d4", name = "Control Booth Door", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 6.5f, promptText = "Enter Control Booth",
                baseDescription = "Monitors hum with your own voice.", isDoor = true, doorGridX = 3, doorGridY = 6
            ),
            InteractiveObject(
                id = "c8_d5", name = "Transmission Core Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 6.5f, promptText = "Unseal Transmission Core",
                baseDescription = "Violet light pours around the lock.", isDoor = true, doorGridX = 8, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c8_monitors", name = "Broadcast Wall Monitors", kind = ObjectKind.TV_SCREEN,
                gridX = 1.5f, gridY = 7.5f, promptText = "Inspect Broadcast Feed",
                baseDescription = "Every screen shows you waking up in the dark briefing room—over and over, across 48 prior resets.",
                emfStrength = 0.99f, realityDistortion = 0.99f,
                linkedEvidenceId = "c8_ev_loop"
            ),
            InteractiveObject(
                id = "c8_director_order", name = "Encrypted Director Directive", kind = ObjectKind.COMPUTER,
                gridX = 8.5f, gridY = 1.5f, promptText = "Read Director Directive",
                baseDescription = "'Agent 049: Purge Channel 6 and seal all files. Your promotion to Director awaits.'",
                linkedEvidenceId = "c8_ev_directive"
            ),
            InteractiveObject(
                id = "c8_master_tape", name = "Master Tape #000 (Anchor Desk)", kind = ObjectKind.FLOOR_CLUE,
                gridX = 5.5f, gridY = 7.5f, requiresCrouch = true,
                promptText = "Extract Master Tape (Crouched)",
                baseDescription = "Unredacted recording of Case #000: DUI manufactured the anomalies to harvest human perception—and Subject 050 left this tape for you to broadcast.",
                audioSnippet = "Voice (050): 'Wake up, 049. Choose: expose them, join them, or step through the frame.'",
                linkedEvidenceId = "c8_ev_secret_master"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c8_ev_loop", title = "48-Cycle Reset Broadcast",
                category = EvidenceCategory.DIGITAL,
                summary = "Proves DUI has reset Investigator 049 forty-eight times.",
                detailedAnalysis = "Channel 6 transmits leaked memories whenever containment weakens.",
                anomalyCategory = AnomalyCategory.TEMPORAL
            ),
            EvidenceDefinition(
                id = "c8_ev_directive", title = "Director's Promotion Order",
                category = EvidenceCategory.DIGITAL,
                summary = "DUI offers full command if you classify Channel 6 and silence the signal.",
                detailedAnalysis = "Choosing Containment or Observation preserves the Department's secrecy.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c8_ev_secret_master", title = "CLASSIFIED: Master Broadcast #000",
                category = EvidenceCategory.AUDIO,
                summary = "Full unredacted proof of Project Mirror-0 and Subject 050's message.",
                detailedAnalysis = "Submitting Internal Audit / Declassify exposes DUI globally.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_obs_08", type = EntityType.THE_OBSERVER,
                startX = 5.5f, startY = 4.5f,
                patrolPoints = listOf(1.5f to 4.5f, 9.5f to 4.5f),
                disappearsOnApproach = false, baseHeat = 0.3f
            ),
            SceneEntity(
                id = "ent_arch_08", type = EntityType.THE_ARCHIVIST,
                startX = 9.5f, startY = 7.5f,
                patrolPoints = listOf(9.5f to 7.5f, 9.5f to 8.5f),
                baseHeat = 0.4f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c8_truth",
                requiredEvidenceIds = setOf("c8_ev_loop", "c8_ev_secret_master"),
                title = "The Final Truth",
                conclusion = "DUI farms anomalies and resets investigators; the broadcast is Subject 050's rescue signal.",
                isValidDeduction = true, unlocksSecretClue = true
            ),
            TheoryHypothesis(
                id = "th_c8_order",
                requiredEvidenceIds = setOf("c8_ev_loop", "c8_ev_directive"),
                title = "Preserve Sector 04",
                conclusion = "Seal the breach and assume the Director's mantle to keep humanity unaware.",
                isValidDeduction = true
            )
        ),
        correctClassification = AnomalyClassification.UNCLASSIFIED_PHENOMENON,
        correctThreatLevel = ThreatLevel.CLASS_V,
        correctAction = RecommendedAction.INTERNAL_AUDIT,
        conspiracyRevealText = "The final signal resolves—your choice in the Investigation Report seals the fate of DUI Sector 04.",
        unlockRequirementCaseId = "case_007"
    )

    val allStoryCases: List<CaseDefinition> = CaseCatalog.firstFourCases + listOf(
        case005, case006, case007, case008
    )

    fun getCaseById(id: String): CaseDefinition? =
        allStoryCases.find { it.id == id }

    fun generateEndlessCase(seed: Int = Random.nextInt(1000, 9999)): CaseDefinition {
        val template = allStoryCases[seed % allStoryCases.size]
        val anomalyPick = listOf(
            AnomalyClassification.INTELLIGENT_ANOMALY,
            AnomalyClassification.DANGEROUS_ANOMALY,
            AnomalyClassification.UNKNOWN_ANOMALY,
            AnomalyClassification.HUMAN_DECEPTION
        )[seed % 4]

        val prefix = "ENDLESS #$seed"
        val customSummary = when (anomalyPick) {
            AnomalyClassification.HUMAN_DECEPTION ->
                "Procedural Dispatch: Local sensors flagged unusual thermal readings at ${template.locationName}, though field chatter hints someone staged the alarms to cover theft."
            AnomalyClassification.INTELLIGENT_ANOMALY ->
                "Procedural Dispatch: An adaptive entity inside ${template.locationName} is actively altering surveillance timestamps and reacting to investigator equipment."
            else ->
                "Procedural Dispatch: Unscheduled spatial and temporal distortions detected at ${template.locationName}. Collect evidence before containment decays."
        }
        return template.copy(
            id = "endless_$seed",
            caseNumber = prefix,
            title = "SECTOR BREACH // ${template.title}",
            incidentSummary = customSummary,
            correctClassification = anomalyPick,
            isStoryCase = false,
            unlockRequirementCaseId = null
        )
    }
}
