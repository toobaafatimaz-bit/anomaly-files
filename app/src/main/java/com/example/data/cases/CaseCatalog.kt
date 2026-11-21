package com.example.data.cases

import com.example.model.*

object CaseCatalog {

    val allAchievements: List<AchievementInfo> = listOf(
        AchievementInfo("ach_first_case", "First Redacted File", "Complete Case #001: The Empty Apartment."),
        AchievementInfo("ach_fifth_figure", "Who Stands Behind You?", "Discover the hidden figure in your personnel photograph."),
        AchievementInfo("ach_perfect_s", "Forensic Precision", "Earn an S-Rank on any investigation report."),
        AchievementInfo("ach_outsmart_entity", "Counter-Adaptation", "Outsmart an Intelligent Anomaly after it adapts to your equipment."),
        AchievementInfo("ach_crouch_clue", "Beneath Eye Level", "Uncover hidden physical evidence while crouched."),
        AchievementInfo("ach_board_master", "Pattern Recognition", "Verify a complete hypothesis on the Investigation Board."),
        AchievementInfo("ach_conspiracy_3", "Deep Clearance", "Uncover 3 classified conspiracy fragments across cases."),
        AchievementInfo("ach_endless_run", "Night Shift Never Ends", "Complete an Endless Investigation file."),
        AchievementInfo("ach_challenge_win", "Under Protocol", "Complete a Challenge Mode investigation."),
        AchievementInfo("ach_secret_ending", "The Fifth Resident", "Unlock the Secret Ending by uncovering the full conspiracy.")
    )

    // CASE 001: THE EMPTY APARTMENT
    // Grid: 12 cols x 10 rows
    // Row 0: ############
    // Row 1: #P...D...M.#  -> Entrance (1.5,1.5), Hallway, Door at (5,1), Master Bedroom + Mirror at (9,1)
    // Row 2: #....#.....#
    // Row 3: ###D##..####
    // Row 4: #....#.....#  -> Living Room (1..4,4..5), Kids Room (6..10,4..5)
    // Row 5: #....D.....#  -> Door at (5,5)
    // Row 6: ####S#######  -> Secret 5th Bedroom Door 'S' at (4,6)!
    // Row 7: #...#......#  -> Impossible 5th Bedroom (1..3,7..8) & Utility alcove (5..10,7..8)
    // Row 8: #...D......#  -> Door at (4,8)
    // Row 9: ############
    private val case001 = CaseDefinition(
        id = "case_001",
        caseNumber = "CASE #001",
        title = "THE EMPTY APARTMENT",
        locationName = "Blackwood Residential Complex — Apt 4B",
        incidentSummary = "A family of four reported footsteps pacing inside Apartment 4B every night at 03:17. Building lobby CCTV recorded five distinct silhouettes entering Unit 4B over three consecutive nights—yet the fifth individual never exits, and the apartment blueprint only lists four rooms.",
        witnessCount = 4,
        suspiciousActivities = listOf(
            "CCTV logs 5 silhouettes entering; only 4 residents exist",
            "Nightly pacing footsteps at 03:17 AM",
            "Unlisted doorframe temperature drops to 4°C",
            "Family portrait altered between police visits"
        ),
        threatClassification = ThreatLevel.CLASS_III,
        primaryObjective = "Discover the identity and nature of the fifth individual inside Apartment 4B.",
        optionalSecretObjective = "Inspect beneath the floorboards near the unlisted wall and scan the 5th door with Spectral Lens.",
        wallTheme = WallTheme.APARTMENT,
        mapGrid = listOf(
            "############",
            "#P...D...M.#",
            "#....#.....#",
            "###D##..####",
            "#....#.....#",
            "#....D.....#",
            "####S#######",
            "#...#......#",
            "#...D......#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c1_door_1",
                name = "Master Hallway Door",
                kind = ObjectKind.DOOR,
                gridX = 5.5f, gridY = 1.5f,
                promptText = "Open Hallway Door",
                baseDescription = "Heavy oak interior door.",
                isDoor = true, doorGridX = 5, doorGridY = 1
            ),
            InteractiveObject(
                id = "c1_door_2",
                name = "Living Room Door",
                kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f,
                promptText = "Open Living Room Door",
                baseDescription = "Standard apartment door with chipped white paint.",
                isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c1_door_3",
                name = "Nursery Partition Door",
                kind = ObjectKind.DOOR,
                gridX = 5.5f, gridY = 5.5f,
                promptText = "Open Nursery Door",
                baseDescription = "Leads between living room and children's bedroom.",
                isDoor = true, doorGridX = 5, doorGridY = 5
            ),
            InteractiveObject(
                id = "c1_secret_door",
                name = "Unlisted Wall Seam / 5th Door",
                kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 6.5f,
                promptText = "Force Open Impossible Door",
                baseDescription = "The architectural blueprint shows solid concrete here, yet a narrow door frame breathes cold air.",
                spectralDescription = "UV markings scratched along the jamb: 'ROOM 05 — DO NOT COUNT'.",
                thermalDescription = "Dead zero heat signature along the hinges—4.1°C.",
                realityDistortion = 0.85f,
                emfStrength = 0.8f,
                linkedEvidenceId = "c1_ev_5th_room",
                isDoor = true, doorGridX = 4, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c1_door_4",
                name = "Back Alcove Door",
                kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 8.5f,
                promptText = "Open Alcove Door",
                baseDescription = "Connects the impossible fifth room to the rear utility corridor.",
                isDoor = true, doorGridX = 4, doorGridY = 8
            ),
            InteractiveObject(
                id = "c1_family_photo",
                name = "Mantelpiece Family Photograph",
                kind = ObjectKind.PHOTOGRAPH,
                gridX = 2.5f, gridY = 4.5f,
                promptText = "Examine Family Photograph",
                baseDescription = "Framed portrait of Elena, Mark, and their two children. At first glance, four people smile at the camera.",
                spectralDescription = "Under Spectral Lens, a fifth taller figure stands behind the couch—wearing a grey DUI coat. Their face is scratched out.",
                memoryEchoDescription = "Echo playback: Elena whispers 'Who took this photo? All four of us are in the frame.'",
                realityDistortion = 0.55f,
                linkedEvidenceId = "c1_ev_photo",
                alternateEvidenceIfAdapted = "c1_ev_redherring_neighbor"
            ),
            InteractiveObject(
                id = "c1_cctv_laptop",
                name = "Lobby Security Terminal",
                kind = ObjectKind.COMPUTER,
                gridX = 1.5f, gridY = 5.5f,
                promptText = "Inspect CCTV Terminal",
                baseDescription = "Corridor camera log 02:58–03:20. Five figures cross the threshold of 4B. The 5th silhouette has no shadow.",
                spectralDescription = "Metadata timestamp reads Year: 1999—long before Blackwood Complex was built.",
                emfStrength = 0.72f,
                linkedEvidenceId = "c1_ev_cctv",
                alternateEvidenceIfAdapted = "c1_ev_cctv_tampered"
            ),
            InteractiveObject(
                id = "c1_mirror",
                name = "Bedroom Vanity Mirror",
                kind = ObjectKind.MIRROR,
                gridX = 9.5f, gridY = 1.5f,
                promptText = "Inspect Mirror Reflection",
                baseDescription = "Dusty wall mirror facing the master bed. Your reflection blinks a fraction of a second too late.",
                spectralDescription = "In UV spectrum, chalk handprints press from INSIDE the glass.",
                thermalDescription = "Glass radiates 36.8°C human body heat despite the empty room.",
                realityDistortion = 0.75f,
                linkedEvidenceId = "c1_ev_mirror"
            ),
            InteractiveObject(
                id = "c1_clock",
                name = "Frozen Wall Clock",
                kind = ObjectKind.CLOCK,
                gridX = 8.5f, gridY = 4.5f,
                promptText = "Examine Wall Clock",
                baseDescription = "Battery-powered analog clock stuck at 03:17. Wait—the second hand ticks once backwards every four seconds.",
                audioSnippet = "Audio Analyzer isolates a rhythmic whisper synced to the backward tick: 'Count again... five...'",
                realityDistortion = 0.65f,
                linkedEvidenceId = "c1_ev_clock"
            ),
            InteractiveObject(
                id = "c1_witness_elena",
                name = "Witness Dictaphone (Elena Vance)",
                kind = ObjectKind.AUDIO_TAPE,
                gridX = 7.5f, gridY = 5.5f,
                promptText = "Replay Witness Statement",
                baseDescription = "Recorded testimony from resident Elena Vance before evacuation.",
                audioSnippet = "EVP layer beneath her voice repeats footsteps matching your exact walking cadence.",
                witnessName = "Elena Vance (Resident)",
                witnessStatement = "We thought it was pipes knocking at night. Then my daughter asked why the tall man in the hallway never casts a shadow. We only have two bedrooms—where is he sleeping?",
                linkedEvidenceId = "c1_ev_witness"
            ),
            InteractiveObject(
                id = "c1_floor_scratch",
                name = "Baseboard Scrape Marks",
                kind = ObjectKind.FLOOR_CLUE,
                gridX = 2.5f, gridY = 7.5f,
                requiresCrouch = true,
                promptText = "Inspect Floor Seam (Crouched)",
                baseDescription = "Hidden behind the radiator inside the fifth bedroom: a torn DUI badge corner stamped 'PROJECT MIRROR-0 // SUBJECT 049'.",
                spectralDescription = "Glowing ink reveals your own investigator serial number printed on the back.",
                emfStrength = 0.9f,
                realityDistortion = 0.95f,
                linkedEvidenceId = "c1_ev_secret_badge"
            ),
            InteractiveObject(
                id = "c1_radiator_vent",
                name = "Loose Heating Vent",
                kind = ObjectKind.BELONGING,
                gridX = 9.5f, gridY = 7.5f,
                promptText = "Check Heating Vent",
                baseDescription = "An old maintenance receipt from 2024 for 'draft sealing'—could explain knocking pipes, though it doesn't explain the 5th silhouette.",
                linkedEvidenceId = "c1_ev_redherring_pipes"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c1_ev_photo",
                title = "Altered Family Photograph",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Four residents visible normally; Spectral Lens exposes a fifth figure in a DUI coat.",
                detailedAnalysis = "Silver-halide emulsion shifted locally around the doorway. Someone—or something—inserted itself into domestic memory.",
                spectralNote = "Fifth figure matches Investigator height profile (181cm).",
                anomalyCategory = AnomalyCategory.VISUAL
            ),
            EvidenceDefinition(
                id = "c1_ev_cctv",
                title = "Lobby CCTV Log #04B",
                category = EvidenceCategory.DIGITAL,
                summary = "Five silhouettes enter Apt 4B at 03:17; fifth casts zero shadow.",
                detailedAnalysis = "Frame-by-frame analysis confirms 5 distinct gait cycles entering, 4 exiting the next morning.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c1_ev_cctv_tampered",
                title = "Corrupted CCTV Stream",
                category = EvidenceCategory.DIGITAL,
                summary = "The anomaly detected repeated terminal checks and overwrote the 5th figure with static.",
                detailedAnalysis = "Timestamps now display your own badge ID accessing the corridor at 03:17.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c1_ev_5th_room",
                title = "Impossible Fifth Bedroom",
                category = EvidenceCategory.ENVIRONMENTAL,
                summary = "A 3x3m room exists inside concrete load-bearing space absent from municipal blueprints.",
                detailedAnalysis = "Laser measurement shows interior volume exceeds exterior building footprint by 11.4 cubic meters.",
                anomalyCategory = AnomalyCategory.SPATIAL
            ),
            EvidenceDefinition(
                id = "c1_ev_mirror",
                title = "Desynced Vanity Reflection",
                category = EvidenceCategory.PHYSICAL,
                summary = "Reflection lags 340ms behind movement and holds 36.8°C surface heat.",
                detailedAnalysis = "Glass acts as an observation membrane rather than passive silvered silica.",
                anomalyCategory = AnomalyCategory.VISUAL
            ),
            EvidenceDefinition(
                id = "c1_ev_clock",
                title = "Backward-Ticking Clock",
                category = EvidenceCategory.PHYSICAL,
                summary = "Analog clock frozen at 03:17 ticks backwards every 4 seconds.",
                detailedAnalysis = "Local temporal loop anchoring the nightly manifestation window.",
                audioTranscript = "'Count again... five...'",
                anomalyCategory = AnomalyCategory.TEMPORAL
            ),
            EvidenceDefinition(
                id = "c1_ev_witness",
                title = "Elena Vance Testimony",
                category = EvidenceCategory.WITNESS,
                summary = "Child saw a shadowless tall man pacing outside a room that shouldn't exist.",
                detailedAnalysis = "Acoustic EVP confirms footsteps match an investigator's stride.",
                anomalyCategory = AnomalyCategory.BIOLOGICAL
            ),
            EvidenceDefinition(
                id = "c1_ev_redherring_pipes",
                title = "Maintenance Pipe Receipt",
                category = EvidenceCategory.PHYSICAL,
                summary = "2024 radiator repair invoice noting loud thermal expansion knocks.",
                detailedAnalysis = "Explains minor knocking sounds, but cannot account for the CCTV figure or spatial anomaly.",
                anomalyCategory = AnomalyCategory.LOGICAL_EXPLANATION,
                isMisleadingRedHerring = true
            ),
            EvidenceDefinition(
                id = "c1_ev_redherring_neighbor",
                title = "Neighbor Miscount Note",
                category = EvidenceCategory.WITNESS,
                summary = "Misleading decoy planted along repeated hallway route suggesting a sleepwalking neighbor.",
                detailedAnalysis = "Handwriting matches no building tenant.",
                anomalyCategory = AnomalyCategory.LOGICAL_EXPLANATION,
                isMisleadingRedHerring = true
            ),
            EvidenceDefinition(
                id = "c1_ev_secret_badge",
                title = "CLASSIFIED: Torn DUI Tag #049",
                category = EvidenceCategory.PHYSICAL,
                summary = "Hidden beneath floorboard: 'PROJECT MIRROR-0 // SUBJECT 049' — your own serial number.",
                detailedAnalysis = "Why was your badge fragment already inside an apartment you supposedly entered for the first time tonight?",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_copy_01",
                type = EntityType.THE_COPY,
                startX = 7.5f, startY = 7.5f,
                patrolPoints = listOf(7.5f to 7.5f, 9.5f to 7.5f, 9.5f to 8.5f, 6.5f to 8.5f),
                disappearsOnApproach = false,
                visibleOnlyInSpectralOrMirror = false,
                baseHeat = 0.85f
            ),
            SceneEntity(
                id = "ent_obs_01",
                type = EntityType.THE_OBSERVER,
                startX = 10.5f, startY = 1.5f,
                patrolPoints = listOf(10.5f to 1.5f, 10.5f to 4.5f),
                disappearsOnApproach = true,
                visibleOnlyInSpectralOrMirror = false,
                baseHeat = 0.25f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c1_main",
                requiredEvidenceIds = setOf("c1_ev_photo", "c1_ev_5th_room"),
                title = "Spatial Mimic Residence",
                conclusion = "The fifth individual is an Intelligent Spatial Anomaly masquerading as a resident inside an impossible unlisted room.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c1_conspiracy",
                requiredEvidenceIds = setOf("c1_ev_secret_badge", "c1_ev_cctv"),
                title = "Pre-Existing Investigator Presence",
                conclusion = "Your own DUI serial tag predates this assignment—suggesting you were the fifth silhouette.",
                isValidDeduction = true,
                unlocksSecretClue = true
            ),
            TheoryHypothesis(
                id = "th_c1_false",
                requiredEvidenceIds = setOf("c1_ev_redherring_pipes", "c1_ev_witness"),
                title = "Faulty Plumbing Hypothesis",
                conclusion = "Old heating pipes caused auditory hallucinations. (Unsupported by CCTV & spatial measurements.)",
                isValidDeduction = false
            )
        ),
        correctClassification = AnomalyClassification.INTELLIGENT_ANOMALY,
        correctThreatLevel = ThreatLevel.CLASS_III,
        correctAction = RecommendedAction.CONTAIN,
        conspiracyRevealText = "Returning to Sector 04, you examine your own Personnel Photograph on the desk. Standing behind your left shoulder is a blurred silhouette nobody else in the office can see."
    )

    // CASE 002: BLACKWOOD HOTEL
    private val case002 = CaseDefinition(
        id = "case_002",
        caseNumber = "CASE #002",
        title = "BLACKWOOD HOTEL",
        locationName = "Blackwood Grand Hotel — Unlisted Floor 13",
        incidentSummary = "Night auditors report guests checking into Rooms 1304 and 1309—yet the elevator skips Floor 13, and room keys returned at dawn belong to people missing since 1978.",
        witnessCount = 3,
        suspiciousActivities = listOf(
            "Guest ledger updates itself in wet ink",
            "Corridor loops back when walking west",
            "Room 1309 exists on both sides of the hall"
        ),
        threatClassification = ThreatLevel.CLASS_III,
        primaryObjective = "Trace how guests reach Floor 13 and identify the entity issuing room keys.",
        optionalSecretObjective = "Use Memory Recorder at the Reception Desk to reconstruct the 1978 check-in.",
        wallTheme = WallTheme.HOTEL,
        mapGrid = listOf(
            "############",
            "#P...#..M..#",
            "#....D.....#",
            "###D####D###",
            "#..........#",
            "#..##..##..#",
            "#..D....D..#",
            "#..........#",
            "#....M.....#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c2_door_1", name = "Lobby Service Door", kind = ObjectKind.DOOR,
                gridX = 5.5f, gridY = 2.5f, promptText = "Open Service Door",
                baseDescription = "Brass plaque reads 'FLOOR 13'.", isDoor = true, doorGridX = 5, doorGridY = 2
            ),
            InteractiveObject(
                id = "c2_door_2", name = "Room 1304 Door", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f, promptText = "Open Room 1304",
                baseDescription = "Keycard slot emits faint violet sparks.", isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c2_door_3", name = "Room 1309 Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Open Room 1309",
                baseDescription = "Door handle feels ice cold.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c2_door_4", name = "Suite Alcove Left", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 6.5f, promptText = "Open Suite Left",
                baseDescription = "Heavy velvet curtain frames the doorway.", isDoor = true, doorGridX = 3, doorGridY = 6
            ),
            InteractiveObject(
                id = "c2_door_5", name = "Suite Alcove Right", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 6.5f, promptText = "Open Suite Right",
                baseDescription = "Identical brass plaque also reads '1309'.", isDoor = true, doorGridX = 8, doorGridY = 6
            ),
            InteractiveObject(
                id = "c2_ledger", name = "Front Desk Guest Ledger", kind = ObjectKind.DOCUMENT,
                gridX = 2.5f, gridY = 1.5f, promptText = "Inspect Guest Ledger",
                baseDescription = "Names signed in 1978 appear on tonight's date—including 'AGENT-049, CHECK-IN PENDING'.",
                spectralDescription = "UV scan shows the ink shifts letters while you aren't looking directly at the page.",
                memoryEchoDescription = "Echo: A bellhop with no facial features hands over brass key #1309.",
                linkedEvidenceId = "c2_ev_ledger"
            ),
            InteractiveObject(
                id = "c2_mirror_hall", name = "Hallway Gilded Mirror", kind = ObjectKind.MIRROR,
                gridX = 8.5f, gridY = 1.5f, promptText = "Inspect Gilded Mirror",
                baseDescription = "The reflection shows a crowded 1978 hotel lobby even though the hall behind you is deserted.",
                thermalDescription = "Cold geometric seam along the frame indicates replacement architecture.",
                linkedEvidenceId = "c2_ev_mirror"
            ),
            InteractiveObject(
                id = "c2_luggage", name = "Unclaimed Suitcase (1978)", kind = ObjectKind.BELONGING,
                gridX = 2.5f, gridY = 7.5f, promptText = "Search Suitcase",
                baseDescription = "Inside lies a newspaper dated November 21, 1978, and an intact DUI magnetic keycard.",
                linkedEvidenceId = "c2_ev_suitcase"
            ),
            InteractiveObject(
                id = "c2_phone", name = "Housekeeping Intercom", kind = ObjectKind.AUDIO_TAPE,
                gridX = 9.5f, gridY = 7.5f, promptText = "Listen to Intercom",
                baseDescription = "Dead landline crackling with lobby music from four decades ago.",
                audioSnippet = "Voice on line: 'Room 1309 is ready. Replacement protocol ninety-percent complete.'",
                emfStrength = 0.85f,
                linkedEvidenceId = "c2_ev_intercom"
            ),
            InteractiveObject(
                id = "c2_carpet_seam", name = "Torn Carpet Underlay", kind = ObjectKind.FLOOR_CLUE,
                gridX = 6.5f, gridY = 8.5f, requiresCrouch = true,
                promptText = "Inspect Carpet Seam (Crouched)",
                baseDescription = "Beneath the carpet, concrete stenciling reads: 'DUI CONTAINMENT WING — REPURPOSED 1981'.",
                linkedEvidenceId = "c2_ev_secret_stencil"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c2_ev_ledger", title = "Self-Updating Guest Ledger",
                category = EvidenceCategory.PHYSICAL,
                summary = "1978 missing guests re-signed tonight; includes Agent-049.",
                detailedAnalysis = "Graphology matches the original 1978 clerk who vanished the night Floor 13 was bricked over.",
                anomalyCategory = AnomalyCategory.TEMPORAL
            ),
            EvidenceDefinition(
                id = "c2_ev_mirror", title = "1978 Lobby Reflection",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Mirror reflects a populated 1978 corridor instead of the empty hall.",
                detailedAnalysis = "Photonic delay suggests overlapping spatial layers.",
                anomalyCategory = AnomalyCategory.SPATIAL
            ),
            EvidenceDefinition(
                id = "c2_ev_suitcase", title = "Preserved 1978 Suitcase",
                category = EvidenceCategory.PHYSICAL,
                summary = "Contains 1978 newsprint and a modern DUI clearance badge.",
                detailedAnalysis = "DUI magnetic stripe encoded with Sector 04 facility keys.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c2_ev_intercom", title = "Intercom Replacement Broadcast",
                category = EvidenceCategory.AUDIO,
                summary = "Sub-audible transmission references 'Replacement protocol 90% complete'.",
                detailedAnalysis = "Entity is systematically swapping missing guests with geometric duplicates.",
                anomalyCategory = AnomalyCategory.BIOLOGICAL
            ),
            EvidenceDefinition(
                id = "c2_ev_secret_stencil", title = "CLASSIFIED: DUI Stencil (1981)",
                category = EvidenceCategory.ENVIRONMENTAL,
                summary = "Floor 13 was secretly converted into an early DUI holding wing in 1981.",
                detailedAnalysis = "DUI didn't discover Blackwood Hotel—they built the anomaly floor.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_rep_02", type = EntityType.THE_REPLACEMENT,
                startX = 5.5f, startY = 4.5f,
                patrolPoints = listOf(1.5f to 4.5f, 9.5f to 4.5f, 9.5f to 7.5f, 1.5f to 7.5f),
                baseHeat = 0.2f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c2_main",
                requiredEvidenceIds = setOf("c2_ev_ledger", "c2_ev_intercom"),
                title = "Replacement Loop Protocol",
                conclusion = "The Replacement lures targets into an overlapping 1978 floor layer and substitutes them.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c2_secret",
                requiredEvidenceIds = setOf("c2_ev_secret_stencil", "c2_ev_suitcase"),
                title = "DUI Architectural Cover-Up",
                conclusion = "The Department constructed Floor 13 in 1981 and abandoned its test subjects.",
                isValidDeduction = true, unlocksSecretClue = true
            )
        ),
        correctClassification = AnomalyClassification.DANGEROUS_ANOMALY,
        correctThreatLevel = ThreatLevel.CLASS_IV,
        correctAction = RecommendedAction.SEAL_SITE,
        conspiracyRevealText = "The hotel blueprint carries the same architect signature as DUI Headquarters Sector 04.",
        unlockRequirementCaseId = "case_001"
    )

    // CASE 003: THE FORGOTTEN SCHOOL
    private val case003 = CaseDefinition(
        id = "case_003",
        caseNumber = "CASE #003",
        title = "THE FORGOTTEN SCHOOL",
        locationName = "St. Jude Preparatory (Condemned 1984)",
        incidentSummary = "Demolition crews uncovered boxes of class portraits from 1952—sixteen years before St. Jude Preparatory was founded. Several students in the 1952 photo match missing children from 2021.",
        witnessCount = 2,
        suspiciousActivities = listOf(
            "Chalkboards rewrite historical dates overnight",
            "School bell rings silently on audio monitors",
            "Archive filing cabinets rearrange alphabetically on their own"
        ),
        threatClassification = ThreatLevel.CLASS_III,
        primaryObjective = "Determine how photographs from 1952 depict students from 2021.",
        optionalSecretObjective = "Inspect the darkroom trays and the loose floor tile beneath the teacher's podium.",
        wallTheme = WallTheme.SCHOOL,
        mapGrid = listOf(
            "############",
            "#P...#.....#",
            "#....D.....#",
            "####D###D###",
            "#....#.....#",
            "#....#.....#",
            "#..D.#..S..#",
            "#..........#",
            "#....M.....#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c3_d1", name = "Main Hallway Door", kind = ObjectKind.DOOR,
                gridX = 5.5f, gridY = 2.5f, promptText = "Open Hallway Door",
                baseDescription = "Frosted glass door reading 'ARCHIVES'.", isDoor = true, doorGridX = 5, doorGridY = 2
            ),
            InteractiveObject(
                id = "c3_d2", name = "Classroom A Door", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 3.5f, promptText = "Open Classroom A",
                baseDescription = "Dust hangs untouched in the air.", isDoor = true, doorGridX = 4, doorGridY = 3
            ),
            InteractiveObject(
                id = "c3_d3", name = "Principal Office Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Open Principal Office",
                baseDescription = "Lock was pried from the inside.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c3_d4", name = "Darkroom Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 6.5f, promptText = "Open Sealed Darkroom",
                baseDescription = "Red safelight bleeds through the doorframe cracks.", isDoor = true, doorGridX = 8, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c3_d5", name = "Corridor Partition", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 6.5f, promptText = "Open Partition Door",
                baseDescription = "Wooden swing door.", isDoor = true, doorGridX = 3, doorGridY = 6
            ),
            InteractiveObject(
                id = "c3_yearbook", name = "1952 Class Portrait", kind = ObjectKind.PHOTOGRAPH,
                gridX = 8.5f, gridY = 1.5f, promptText = "Examine 1952 Portrait",
                baseDescription = "Sepia photograph stamped '1952'. In the back row sits your own childhood face—alongside a note: 'FIRST COHORT'.",
                spectralDescription = "Spectral Lens shows the date was edited from '1994' to '1952' by an unseen hand.",
                linkedEvidenceId = "c3_ev_portrait"
            ),
            InteractiveObject(
                id = "c3_chalkboard", name = "Classroom Chalkboard", kind = ObjectKind.WALL_MARKING,
                gridX = 2.5f, gridY = 4.5f, promptText = "Read Chalkboard",
                baseDescription = "A multiplication table written cleanly—except every answer equals '03:17'.",
                memoryEchoDescription = "Echo: Children recite attendance names, stopping abruptly at 'Investigator 049'.",
                linkedEvidenceId = "c3_ev_chalk"
            ),
            InteractiveObject(
                id = "c3_tape", name = "Tape Reel Player", kind = ObjectKind.AUDIO_TAPE,
                gridX = 9.5f, gridY = 5.5f, promptText = "Analyze Tape Reel",
                baseDescription = "Magnetic tape spinning backwards inside the principal's drawer.",
                audioSnippet = "Voice: 'The Archivist preserves what the Department deletes. Check the darkroom floor.'",
                emfStrength = 0.78f,
                linkedEvidenceId = "c3_ev_audio"
            ),
            InteractiveObject(
                id = "c3_darkroom_tray", name = "Darkroom Chemical Tray", kind = ObjectKind.FLOOR_CLUE,
                gridX = 9.5f, gridY = 7.5f, requiresCrouch = true,
                promptText = "Inspect Under Darkroom Sink (Crouched)",
                baseDescription = "Waterproof canister stamped 'DUI COHORT MEMORY EXTRACTION — 1994'.",
                linkedEvidenceId = "c3_ev_secret_cohort"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c3_ev_portrait", title = "Anachronistic 1952 Portrait",
                category = EvidenceCategory.PHOTOGRAPHIC,
                summary = "Photo dated 1952 features modern missing children and your childhood likeness.",
                detailedAnalysis = "Spectral ink reveals systematic archival tampering by an entity.",
                anomalyCategory = AnomalyCategory.TEMPORAL
            ),
            EvidenceDefinition(
                id = "c3_ev_chalk", title = "Synchronized Chalkboard Cipher",
                category = EvidenceCategory.ENVIRONMENTAL,
                summary = "Memory echo reveals attendance calls referencing Investigator 049.",
                detailedAnalysis = "Chalk dust contains zero calcium—composed of pulverized magnetic tape oxide.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c3_ev_audio", title = "Archivist Whisper Reel",
                category = EvidenceCategory.AUDIO,
                summary = "Recording claims The Archivist preserves records DUI tries to erase.",
                detailedAnalysis = "Suggests at least one intelligent anomaly is trying to warn you.",
                anomalyCategory = AnomalyCategory.INTELLIGENT
            ),
            EvidenceDefinition(
                id = "c3_ev_secret_cohort", title = "CLASSIFIED: 1994 Cohort Canister",
                category = EvidenceCategory.PHYSICAL,
                summary = "DUI used St. Jude's to erase and train orphaned recruits in 1994.",
                detailedAnalysis = "Your childhood memories weren't lost—they were archived here.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_arch_03", type = EntityType.THE_ARCHIVIST,
                startX = 7.5f, startY = 4.5f,
                patrolPoints = listOf(7.5f to 4.5f, 9.5f to 4.5f, 9.5f to 1.5f, 7.5f to 1.5f),
                baseHeat = 0.35f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c3_main",
                requiredEvidenceIds = setOf("c3_ev_portrait", "c3_ev_audio"),
                title = "Archival Preservation Entity",
                conclusion = "The Archivist alters historical dates to draw attention to erased DUI records.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c3_secret",
                requiredEvidenceIds = setOf("c3_ev_secret_cohort", "c3_ev_chalk"),
                title = "DUI Recruit Conditioning Site",
                conclusion = "St. Jude Preparatory was a secret DUI memory-conditioning school.",
                isValidDeduction = true, unlocksSecretClue = true
            )
        ),
        correctClassification = AnomalyClassification.INTELLIGENT_ANOMALY,
        correctThreatLevel = ThreatLevel.CLASS_III,
        correctAction = RecommendedAction.INTERNAL_AUDIT,
        conspiracyRevealText = "You recognize the classroom desk in the corner—your initials are carved underneath the wood.",
        unlockRequirementCaseId = "case_002"
    )

    // CASE 004: UNDERGROUND STATION 09
    private val case004 = CaseDefinition(
        id = "case_004",
        caseNumber = "CASE #004",
        title = "UNDERGROUND STATION 09",
        locationName = "Decommissioned Subway Line B — Station 09",
        incidentSummary = "Transit sensors log an unscheduled six-car train arriving at Station 09 every night at 03:17. Cameras show the doors opening onto an empty platform, though acoustic sensors detect zero engine noise.",
        witnessCount = 2,
        suspiciousActivities = listOf(
            "Phantom train arrives silently at 03:17",
            "Ticket machine dispenses passes dated tomorrow",
            "EMF spikes peak inside Car 3 despite cut third-rail power"
        ),
        threatClassification = ThreatLevel.CLASS_IV,
        primaryObjective = "Board the stationary platform anomaly and identify what travels aboard the silent train.",
        optionalSecretObjective = "Scan the Signal Relay Box with the Signal Detector and inspect under the platform bench.",
        wallTheme = WallTheme.SUBWAY,
        mapGrid = listOf(
            "############",
            "#P..#......#",
            "#...D......#",
            "###D####D###",
            "#..........#",
            "#..........#",
            "###D####S###",
            "#...#......#",
            "#...D......#",
            "############"
        ),
        objects = listOf(
            InteractiveObject(
                id = "c4_d1", name = "Turnstile Gate", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 2.5f, promptText = "Pass Turnstile Gate",
                baseDescription = "Rusted turnstile clicks open without a token.", isDoor = true, doorGridX = 4, doorGridY = 2
            ),
            InteractiveObject(
                id = "c4_d2", name = "North Stairwell", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 3.5f, promptText = "Open Platform Gate",
                baseDescription = "Leads onto Platform 09.", isDoor = true, doorGridX = 3, doorGridY = 3
            ),
            InteractiveObject(
                id = "c4_d3", name = "Signal Booth Door", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 3.5f, promptText = "Open Signal Booth",
                baseDescription = "Emergency red lamps pulse inside.", isDoor = true, doorGridX = 8, doorGridY = 3
            ),
            InteractiveObject(
                id = "c4_d4", name = "Train Car 3 Door", kind = ObjectKind.DOOR,
                gridX = 3.5f, gridY = 6.5f, promptText = "Board Train Car 3",
                baseDescription = "Subway sliding door frozen halfway open.", isDoor = true, doorGridX = 3, doorGridY = 6
            ),
            InteractiveObject(
                id = "c4_d5", name = "Sealed Track Tunnel", kind = ObjectKind.DOOR,
                gridX = 8.5f, gridY = 6.5f, promptText = "Open Track Bulkhead",
                baseDescription = "Heavy steel door bolted by DUI Maintenance.", isDoor = true, doorGridX = 8, doorGridY = 6,
                isSecretRoomDoor = true
            ),
            InteractiveObject(
                id = "c4_d6", name = "Car Connector", kind = ObjectKind.DOOR,
                gridX = 4.5f, gridY = 8.5f, promptText = "Open Connector Door",
                baseDescription = "Connects Car 3 to Sub-Tunnel.", isDoor = true, doorGridX = 4, doorGridY = 8
            ),
            InteractiveObject(
                id = "c4_ticket", name = "Automated Ticket Kiosk", kind = ObjectKind.COMPUTER,
                gridX = 2.5f, gridY = 1.5f, promptText = "Inspect Ticket Kiosk",
                baseDescription = "Printer spits out a single stub: 'DESTINATION: SECTOR 04 SUB-LEVEL — ONE WAY'.",
                linkedEvidenceId = "c4_ev_ticket"
            ),
            InteractiveObject(
                id = "c4_relay", name = "Signal Relay Terminal", kind = ObjectKind.SECURITY_CAMERA,
                gridX = 8.5f, gridY = 1.5f, promptText = "Inspect Signal Relay",
                baseDescription = "Third rail shows 0 Volts, yet EMF meter hits 98%. The train runs on non-electrical resonance.",
                emfStrength = 0.98f,
                linkedEvidenceId = "c4_ev_emf"
            ),
            InteractiveObject(
                id = "c4_train_seat", name = "Car 3 Passenger Seat", kind = ObjectKind.BELONGING,
                gridX = 2.5f, gridY = 7.5f, promptText = "Inspect Car 3",
                baseDescription = "Twenty empty seats—yet Thermal Vision shows twenty-one cold outlines seated quietly facing forward.",
                thermalDescription = "All silhouettes register 11°C—colder than the tunnel air.",
                audioSnippet = "Audio Analyzer catches synchronized breathing that stops the instant you step inside.",
                linkedEvidenceId = "c4_ev_passengers"
            ),
            InteractiveObject(
                id = "c4_track_plate", name = "Sub-Tunnel Brass Plaque", kind = ObjectKind.FLOOR_CLUE,
                gridX = 7.5f, gridY = 7.5f, requiresCrouch = true,
                promptText = "Read Track Plate (Crouched)",
                baseDescription = "Bolted beneath the rails: 'DUI TRANSIT LINE 0 — SPECIMEN TRANSPORT ONLY'.",
                linkedEvidenceId = "c4_ev_secret_transit"
            )
        ),
        evidencePool = listOf(
            EvidenceDefinition(
                id = "c4_ev_ticket", title = "Future-Dated Transit Stub",
                category = EvidenceCategory.PHYSICAL,
                summary = "Printed destination lists DUI Sector 04 Sub-Level.",
                detailedAnalysis = "Thermal paper reacts before being touched.",
                anomalyCategory = AnomalyCategory.TEMPORAL
            ),
            EvidenceDefinition(
                id = "c4_ev_emf", title = "Zero-Voltage EMF Anomaly",
                category = EvidenceCategory.DIGITAL,
                summary = "98% electromagnetic spike on unpowered tracks.",
                detailedAnalysis = "Resonance frequency matches THE SILENT entity profile.",
                anomalyCategory = AnomalyCategory.SPATIAL
            ),
            EvidenceDefinition(
                id = "c4_ev_passengers", title = "Silent Car 3 Passengers",
                category = EvidenceCategory.WITNESS,
                summary = "Sub-audible breathing detected from invisible cold silhouettes aboard Car 3.",
                detailedAnalysis = "Entities emit zero normal decibels; only EVP Analyzer detects them.",
                anomalyCategory = AnomalyCategory.BIOLOGICAL
            ),
            EvidenceDefinition(
                id = "c4_ev_secret_transit", title = "CLASSIFIED: DUI Transit Line 0",
                category = EvidenceCategory.ENVIRONMENTAL,
                summary = "Station 09 secretly connects directly beneath DUI Headquarters.",
                detailedAnalysis = "The Organization uses the phantom train to move containment subjects unseen.",
                anomalyCategory = AnomalyCategory.INTELLIGENT,
                isConspiracySecret = true
            )
        ),
        entities = listOf(
            SceneEntity(
                id = "ent_silent_04", type = EntityType.THE_SILENT,
                startX = 5.5f, startY = 4.5f,
                patrolPoints = listOf(1.5f to 4.5f, 9.5f to 4.5f, 9.5f to 5.5f, 1.5f to 5.5f),
                baseHeat = 0.1f
            )
        ),
        validTheories = listOf(
            TheoryHypothesis(
                id = "th_c4_main",
                requiredEvidenceIds = setOf("c4_ev_emf", "c4_ev_passengers"),
                title = "Acoustic-Null Entity Transit",
                conclusion = "The Silent rides an electromagnetic phantom train arriving at 03:17.",
                isValidDeduction = true
            ),
            TheoryHypothesis(
                id = "th_c4_secret",
                requiredEvidenceIds = setOf("c4_ev_secret_transit", "c4_ev_ticket"),
                title = "DUI Underground Logistics",
                conclusion = "Station 09 is a covert DUI transport artery masquerading as an urban legend.",
                isValidDeduction = true, unlocksSecretClue = true
            )
        ),
        correctClassification = AnomalyClassification.DANGEROUS_ANOMALY,
        correctThreatLevel = ThreatLevel.CLASS_IV,
        correctAction = RecommendedAction.SEAL_SITE,
        conspiracyRevealText = "Checking the tunnel map at HQ reveals Line B runs directly under your personal office.",
        unlockRequirementCaseId = "case_003"
    )

    val firstFourCases: List<CaseDefinition> = listOf(case001, case002, case003, case004)
}
