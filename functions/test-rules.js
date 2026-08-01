const { initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

async function testRules() {
  const projectId = `test-project-${Date.now()}`;
  const testEnv = await initializeTestEnvironment({
    projectId,
    firestore: { rules: fs.readFileSync(path.join(__dirname, '../firestore.rules'), 'utf8') },
  });

  const alice = testEnv.authenticatedContext('alice');
  const db = alice.firestore();

  // Create group first
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection('groups').doc('group1').set({
      memberIds: ['alice'],
      // Missing status and deletionRequested to simulate legacy group
    });
  });

  try {
    await db.collection('groups').doc('group1').collection('settlements').doc('s1').set({
      amount: 100
    });
    console.log('SUCCESS: Allowed to create settlement in legacy group');
  } catch (e) {
    console.log('FAILURE: Denied creating settlement in legacy group', e.message);
  }

  await testEnv.cleanup();
}

testRules();
