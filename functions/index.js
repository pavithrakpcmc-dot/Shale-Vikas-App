const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

exports.suggestCost = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in before requesting a cost suggestion.");
  }

  const category = String(request.data.category || "Roof");
  const title = String(request.data.title || "School need");
  const description = String(request.data.description || "");

  const baseByCategory = {
    Roof: 45000,
    Furniture: 32000,
    Library: 25000
  };
  const base = baseByCategory[category] || 30000;
  const detailFactor = Math.min(Math.max(description.length / 200, 0), 0.25);
  const estimate = Math.round(base * (1 + detailFactor) / 500) * 500;

  return {
    estimate,
    reason: `Estimated for ${title}: materials, transport, labor and local vendor variation for ${category.toLowerCase()} work.`
  };
});

exports.aggregatePledge = onDocumentCreated("pledges/{pledgeId}", async (event) => {
  const pledge = event.data.data();
  const needId = pledge.needId;
  const alumniId = pledge.alumniId;
  const alumniName = pledge.alumniName || "Alumni";
  const amount = Number(pledge.amount || 0);

  if (!needId || !alumniId || amount <= 0) {
    return;
  }

  const needRef = db.collection("needs").doc(needId);
  const leaderboardRef = db.collection("leaderboard").doc(alumniId);

  await db.runTransaction(async (transaction) => {
    transaction.update(needRef, {
      collectedAmount: admin.firestore.FieldValue.increment(amount),
      pledgeCount: admin.firestore.FieldValue.increment(1)
    });
    transaction.set(leaderboardRef, {
      name: alumniName,
      totalPledged: admin.firestore.FieldValue.increment(amount)
    }, { merge: true });
  });
});
