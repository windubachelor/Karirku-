const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_karirku_123";
const BOB_UID = "bob_karirku_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, created = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    displayName: "Sarah Putri, S.Ds.",
    headline: "Senior Product & UX Designer",
    location: "Jakarta Selatan",
    availabilityStatus: "OPEN_TO_WORK",
    targetRoles: ["Lead UX Designer", "Senior Product Designer"],
    expectedSalaryMin: 20000000,
    expectedSalaryMax: 32000000,
    preferredWorkTypes: ["Hybrid Jakarta", "Remote Global"],
    followUpRemindersEnabled: true,
    darkModeEnabled: false,
    createdAt: created,
    updatedAt: created,
  };
}

function validApplicationPayload(uid, created = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    dossierCode: "#ID-8849",
    roleTitle: "Senior UX Researcher",
    companyName: "Tokopedia Indonesia",
    location: "Jakarta Selatan",
    status: "INTERVIEW",
    workMode: "HYBRID",
    contractType: "FULL_TIME",
    salaryMin: 15000000,
    salaryMax: 25000000,
    appliedDateMillis: Date.now() - 86400000 * 5,
    targetResponseMillis: Date.now() + 86400000 * 7,
    notes: "Memimpin riset evaluatif untuk produk Financial Ecosystem.",
    hrContact: "hr@tokopedia.com",
    attachments: ["CV_SarahPutri_2026.pdf|1.4 MB • Versi ATS Terbaru"],
    starred: true,
    createdAt: created,
    updatedAt: created,
  };
}

function validSchedulePayload(uid, created = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    applicationId: "app_1",
    companyName: "GoTo Financial",
    roleTitle: "Senior Product Designer",
    stageTitle: "Interview HR & User",
    scheduledAtMillis: Date.now() + 86400000 * 2,
    durationMinutes: 60,
    meetingFormat: "ONLINE",
    meetingLocation: "https://meet.google.com/abc-defg-hij",
    interviewerName: "Raka Pratama (Design Lead)",
    prepNotes: "Siapkan studi kasus GoPay design system.",
    completed: false,
    createdAt: created,
    updatedAt: created,
  };
}

function validDocumentPayload(uid, created = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    title: "CV ATS-Friendly 2026",
    category: "CV",
    subtitle: "PDF • 2.4 MB • Diperbarui hari ini",
    externalUrl: "https://portfolio.example.com/cv",
    verified: true,
    createdAt: created,
    updatedAt: created,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any KarirKu collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("applications").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("applications")
      .doc("app_1")
      .set(validApplicationPayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("schedules").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("documents").get()
  );
});

test("Owner can create, read, and partially update UserProfile; ghost & immutable fields rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(profileRef.get());

  // Partial update: identity section
  await assertSucceeds(
    profileRef.update({
      headline: "Lead Product Designer",
      location: "Bandung / Remote",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update: career preferences section
  await assertSucceeds(
    profileRef.update({
      targetRoles: ["Principal Designer"],
      expectedSalaryMin: 25000000,
      expectedSalaryMax: 40000000,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Partial update: settings section
  await assertSucceeds(
    profileRef.update({
      darkModeEnabled: true,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject createdAt mutation
  await assertFails(
    profileRef.update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject ghost field
  await assertFails(
    profileRef.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject future timestamp
  await assertFails(
    profileRef.update({
      headline: "Time Traveler",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject listing /users root collection
  await assertFails(aliceDb.collection("users").get());
});

test("Owner CRUD & partial updates on JobApplication subcollection; rejects @DocumentId id & invalid enums", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const appsCol = aliceDb.collection("users").doc(ALICE_UID).collection("applications");

  await assertSucceeds(appsCol.doc("app_1").set(validApplicationPayload(ALICE_UID)));
  await assertSucceeds(appsCol.get());
  await assertSucceeds(appsCol.doc("app_1").get());

  // Toggle star partial update
  await assertSucceeds(
    appsCol.doc("app_1").update({
      starred: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Status transition partial update
  await assertSucceeds(
    appsCol.doc("app_1").update({
      status: "OFFER",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject writing extra "id" field on create
  await assertFails(
    appsCol.doc("app_bad_id").set({
      id: "app_bad_id",
      ...validApplicationPayload(ALICE_UID),
    })
  );

  // Reject invalid status enum
  await assertFails(
    appsCol.doc("app_1").update({
      status: "HIRED_MAGIC",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject dossierCode mutation
  await assertFails(
    appsCol.doc("app_1").update({
      dossierCode: "#ID-0000",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Owner can delete
  await assertSucceeds(appsCol.doc("app_1").delete());
});

test("Owner CRUD on InterviewSchedule & CareerDocument subcollections", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const schedCol = aliceDb.collection("users").doc(ALICE_UID).collection("schedules");
  const docsCol = aliceDb.collection("users").doc(ALICE_UID).collection("documents");

  await assertSucceeds(schedCol.doc("sched_1").set(validSchedulePayload(ALICE_UID)));
  await assertSucceeds(schedCol.get());
  await assertSucceeds(
    schedCol.doc("sched_1").update({
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(schedCol.doc("sched_1").delete());

  await assertSucceeds(docsCol.doc("doc_1").set(validDocumentPayload(ALICE_UID)));
  await assertSucceeds(docsCol.get());
  await assertSucceeds(
    docsCol.doc("doc_1").update({
      title: "CV ATS-Friendly 2026 v2",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(docsCol.doc("doc_1").delete());
});

test("Cross-user isolation: Alice cannot access or mutate Bob's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("applications")
      .doc("bob_app")
      .set(validApplicationPayload(BOB_UID));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("schedules")
      .doc("bob_sched")
      .set(validSchedulePayload(BOB_UID));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("documents")
      .doc("bob_doc")
      .set(validDocumentPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("applications").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("applications")
      .doc("bob_app")
      .update({ status: "DITOLAK", updatedAt: new Date(Date.now() - 1000) })
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("schedules").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("documents").get()
  );
});
