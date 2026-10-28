const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").get());
});

test("Authenticated user: can read products", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("products").get());
});

test("Customer user: cannot create product (only admin can)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("products").doc("prod1").set({
    name: "VIP Top Up",
    categoryId: "cat1",
    price: 50000,
    stock: 100,
    isActive: true,
    productType: "digital",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Admin user: can create product", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "candrajouva03@gmail.com",
    email_verified: true
  }).firestore();
  await assertSucceeds(adminDb.collection("products").doc("prod1").set({
    name: "VIP Top Up",
    categoryId: "cat1",
    price: 50000,
    stock: 100,
    isActive: true,
    productType: "digital",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Customer user: can create order for themselves", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("orders").doc("order1").set({
    orderCode: "ZX-1001",
    userId: ALICE_UID,
    items: [{ productId: "prod1", name: "VIP Top Up", price: 50000, quantity: 1, subtotal: 50000 }],
    subtotal: 50000,
    total: 50000,
    paymentMethod: "QRIS",
    paymentStatus: "pending",
    orderStatus: "awaiting_payment",
    customerContact: "08123456789",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Customer cannot read someone else's order", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_bob").set({
      orderCode: "ZX-1002",
      userId: BOB_UID,
      items: [{ productId: "prod1", name: "VIP Top Up", price: 50000, quantity: 1, subtotal: 50000 }],
      subtotal: 50000,
      total: 50000,
      paymentMethod: "QRIS",
      paymentStatus: "pending",
      orderStatus: "awaiting_payment",
      customerContact: "08987654321",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("orders").doc("order_bob").get());
});
