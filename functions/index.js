/* eslint-disable no-var */
/* eslint-disable require-jsdoc */
/* eslint-disable no-unused-vars */
/* eslint-disable max-len */
const functions = require("firebase-functions");

// the firebase admin sdk to access cloud firestore
const admin = require("firebase-admin");
admin.initializeApp();

var adjectivesEasyPercentageList = [];
var adjectivesMediumPercentageList = [];
var adjectivesHardPercentageList = [];
var adjectivesSelfAssessmentPercentageList = [];


var nounsEasyPercentageList = [];
var nounsMediumPercentageList = [];
var nounsHardPercentageList = [];
var nounsSelfAssessmentPercentageList = [];


var pronounsEasyPercentageList = [];
var pronounsMediumPercentageList = [];
var pronounsHardPercentageList = [];
var pronounsSelfAssessmentPercentageList = [];


var verbsEasyPercentageList = [];
var verbsMediumPercentageList = [];
var verbsHardPercentageList = [];
var verbsSelfAssessmentPercentageList = [];

var userAverage = 0;

// // Create and deploy your first functions
// // https://firebase.google.com/docs/functions/get-started
//
exports.apiRequest = functions.https.onCall(async (data, context) => {
  const apiKey = "AIzaSyAz87933LARh1wJZ9tIbKFKB5ku4K72Du4";
  // Make a request to the Google NLP API to analyze the syntax of the text
  const response = await fetch(
    `https://language.googleapis.com/v1/documents:analyzeSyntax?key=${apiKey}`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        document: {
          type: "PLAIN_TEXT",
          content: data.myString,
        },
      }),
    },
  );
  // Extract the syntax analysis from the response
  const { tokens } = await response.json();
  return tokens;
});

exports.getName = functions.https.onCall(async (data, context) => {
  // const userRef = db.collection("users").doc(data.userId).get();
  // works
  const readResult = await admin.firestore().collection("users").doc(data.userId).get().then((doc) => {
    console.log(doc.data().firstName);
  });
  // for all documents
  // console.log(readResult.docs.data());
  //   if (readResult.docs) {
  //     readResult.docs.forEach((doc) => {
  //       console.log(doc.data());
  //     });
  //   }
  // console.log(readResult.docs);
  return null;
});

exports.getAverage = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .get();
  // for all documents
  const userPromises = userRef.docs.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userNounsListSize = doc.data().nounsList.length;
    var userVerbsListSize = doc.data().verbsList.length;
    var userPronounsListSize = doc.data().pronounsList.length;
    var userAdjectivesListSize = doc.data().adjectivesList.length;

    var machineNounsListSize = machineDoc.data().nounsList.length;
    var machineVerbsListSize = machineDoc.data().verbsList.length;
    var machinePronounsListSize = machineDoc.data().pronounsList.length;
    var machineAdjectivesListSize = machineDoc.data().adjectivesList.length;

    var adjectivesPercentage = userAdjectivesListSize / machineAdjectivesListSize * 100;
    var nounsPercentage = userNounsListSize / machineNounsListSize * 100;
    var pronounsPercentage = userPronounsListSize / machinePronounsListSize * 100;
    var verbsPercentage = userVerbsListSize / machineVerbsListSize * 100;

    // Add the percentages to the respective lists
    adjectivesPercentageList.push(adjectivesPercentage);
    nounsPercentageList.push(nounsPercentage);
    pronounsPercentageList.push(pronounsPercentage);
    verbsPercentageList.push(verbsPercentage);
  });
  await Promise.all(userPromises);

  var adjectivesAverage = adjectivesPercentageList.reduce((a, b) => a + b, 0) / adjectivesPercentageList.length;
  var nounsAverage = nounsPercentageList.reduce((a, b) => a + b, 0) / nounsPercentageList.length;
  var pronounsAverage = pronounsPercentageList.reduce((a, b) => a + b, 0) / pronounsPercentageList.length;
  var verbsAverage = verbsPercentageList.reduce((a, b) => a + b, 0) / verbsPercentageList.length;

  userAverage = (adjectivesAverage + nounsAverage + pronounsAverage + verbsAverage) / 4;
  return userAverage.toString();
});


// ADJECTIVES
exports.getAdjectivesEasy = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Adjectives")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const easyDocuments = documents.filter((doc) => doc.data().difficulty === "Easy");

  // for all documents
  const userPromises = easyDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Adjectives")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userAdjectivesListSize = doc.data().userAdjectivesList.length;
    var machineAdjectivesListSize = machineDoc.data().adjectivesList.length;

    var adjectivesPercentage = userAdjectivesListSize / machineAdjectivesListSize * 100;
    adjectivesEasyPercentageList.push(adjectivesPercentage);
  });
  await Promise.all(userPromises);

  var adjectivesEasyAverage = Math.floor(adjectivesEasyPercentageList.reduce((a, b) => a + b, 0) / adjectivesEasyPercentageList.length);
  adjectivesEasyPercentageList.length = 0;


  return adjectivesEasyAverage;
});

exports.getAdjectivesMedium = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Adjectives")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;
  // Filter documents where "difficulty" is equal to "Medium"
  const mediumDocuments = documents.filter((doc) => doc.data().difficulty === "Medium");

  // for all documents
  const userPromises = mediumDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Adjectives")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userAdjectivesListSize = doc.data().userAdjectivesList.length;
    var machineAdjectivesListSize = machineDoc.data().adjectivesList.length;

    var adjectivesPercentage = userAdjectivesListSize / machineAdjectivesListSize * 100;
    adjectivesMediumPercentageList.push(adjectivesPercentage);
  });
  await Promise.all(userPromises);

  var adjectivesMediumAverage = Math.floor(adjectivesMediumPercentageList.reduce((a, b) => a + b, 0) / adjectivesMediumPercentageList.length);
  adjectivesMediumPercentageList.length = 0;

  return adjectivesMediumAverage;
});

exports.getAdjectivesHard = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Adjectives")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Hard"
  const hardDocuments = documents.filter((doc) => doc.data().difficulty === "Hard");

  // for all documents
  const userPromises = hardDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Adjectives")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userAdjectivesListSize = doc.data().userAdjectivesList.length;
    var machineAdjectivesListSize = machineDoc.data().adjectivesList.length;

    var adjectivesPercentage = userAdjectivesListSize / machineAdjectivesListSize * 100;
    adjectivesHardPercentageList.push(adjectivesPercentage);
  });
  await Promise.all(userPromises);

  var adjectivesHardAverage = Math.floor(adjectivesHardPercentageList.reduce((a, b) => a + b, 0) / adjectivesHardPercentageList.length);
  adjectivesHardPercentageList.length = 0;

  return adjectivesHardAverage;
});


// VERBS
exports.getVerbsEasy = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Verbs")
    .get();

  // Get the documents in the "Verbs" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const easyDocuments = documents.filter((doc) => doc.data().difficulty === "Easy");

  // for all documents
  const userPromises = easyDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Verbs")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userVerbsListSize = doc.data().userVerbsList.length;
    var machineVerbsListSize = machineDoc.data().verbsList.length;

    var verbsPercentage = userVerbsListSize / machineVerbsListSize * 100;
    verbsEasyPercentageList.push(verbsPercentage);
  });
  await Promise.all(userPromises);

  var verbsEasyAverage = Math.floor(verbsEasyPercentageList.reduce((a, b) => a + b, 0) / verbsEasyPercentageList.length);
  verbsEasyPercentageList.length = 0;

  return verbsEasyAverage;
});

exports.getVerbsMedium = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Verbs")
    .get();

  // Get the documents in the "Verbs" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Medium"
  const mediumDocuments = documents.filter((doc) => doc.data().difficulty === "Medium");

  // for all documents
  const userPromises = mediumDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Verbs")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userVerbsListSize = doc.data().userVerbsList.length;
    var machineVerbsListSize = machineDoc.data().verbsList.length;

    var verbsPercentage = userVerbsListSize / machineVerbsListSize * 100;
    verbsMediumPercentageList.push(verbsPercentage);
  });
  await Promise.all(userPromises);

  var verbsMediumAverage = Math.floor(verbsMediumPercentageList.reduce((a, b) => a + b, 0) / verbsMediumPercentageList.length);
  verbsMediumPercentageList.length = 0;

  return verbsMediumAverage;
});

exports.getVerbsHard = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Verbs")
    .get();

  // Get the documents in the "Verbs" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const hardDocuments = documents.filter((doc) => doc.data().difficulty === "Hard");

  // for all documents
  const userPromises = hardDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Verbs")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userVerbsListSize = doc.data().userVerbsList.length;
    var machineVerbsListSize = machineDoc.data().verbsList.length;

    var verbsPercentage = userVerbsListSize / machineVerbsListSize * 100;
    verbsHardPercentageList.push(verbsPercentage);
  });
  await Promise.all(userPromises);

  var verbsHardAverage = Math.floor(verbsHardPercentageList.reduce((a, b) => a + b, 0) / verbsHardPercentageList.length);
  verbsHardPercentageList.length = 0;

  return verbsHardAverage;
});


// NOUNS
exports.getNounsEasy = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Nouns")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const easyDocuments = documents.filter((doc) => doc.data().difficulty === "Easy");

  // for all documents
  const userPromises = easyDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Nouns")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userNounsListSize = doc.data().userNounsList.length;
    var machineNounsListSize = machineDoc.data().adjectivesList.length;

    var nounsPercentage = userNounsListSize / machineNounsListSize * 100;
    nounsEasyPercentageList.push(nounsPercentage);
  });
  await Promise.all(userPromises);

  var nounsEasyAverage = Math.floor(nounsEasyPercentageList.reduce((a, b) => a + b, 0) / nounsEasyPercentageList.length);
  nounsEasyPercentageList.length = 0;

  return nounsEasyAverage;
});

exports.getNounsMedium = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Nouns")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Medium"
  const mediumDocuments = documents.filter((doc) => doc.data().difficulty === "Medium");

  // for all documents
  const userPromises = mediumDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Nouns")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userNounsListSize = doc.data().userNounsList.length;
    var machineNounsListSize = machineDoc.data().adjectivesList.length;

    var nounsPercentage = userNounsListSize / machineNounsListSize * 100;
    nounsMediumPercentageList.push(nounsPercentage);
  });
  await Promise.all(userPromises);

  var nounsMediumAverage = Math.floor(nounsMediumPercentageList.reduce((a, b) => a + b, 0) / nounsMediumPercentageList.length);
  nounsMediumPercentageList.length = 0;

  return nounsMediumAverage;
});

exports.getNounsHard = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Nouns")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Hard"
  const hardDocuments = documents.filter((doc) => doc.data().difficulty === "Hard");

  // for all documents
  const userPromises = hardDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Nouns")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userNounsListSize = doc.data().userNounsList.length;
    var machineNounsListSize = machineDoc.data().adjectivesList.length;

    var nounsPercentage = userNounsListSize / machineNounsListSize * 100;
    nounsHardPercentageList.push(nounsPercentage);
  });
  await Promise.all(userPromises);

  var nounsHardAverage = Math.floor(nounsHardPercentageList.reduce((a, b) => a + b, 0) / nounsHardPercentageList.length);
  nounsHardPercentageList.length = 0;

  return nounsHardAverage;
});

// PRONOUNS
exports.getPronounsEasy = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Pronouns")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const easyDocuments = documents.filter((doc) => doc.data().difficulty === "Easy");

  // for all documents
  const userPromises = easyDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Pronouns")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userPronounsListSize = doc.data().userPronounsList.length;
    var machinePronounsListSize = machineDoc.data().pronounsList.length;

    var pronounsPercentage = userPronounsListSize / machinePronounsListSize * 100;
    pronounsEasyPercentageList.push(pronounsPercentage);
  });
  await Promise.all(userPromises);

  var pronounsEasyAverage = Math.floor(pronounsEasyPercentageList.reduce((a, b) => a + b, 0) / pronounsEasyPercentageList.length);
  pronounsEasyPercentageList.length = 0;

  return pronounsEasyAverage;
});

exports.getPronounsMedium = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Pronouns")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const easyDocuments = documents.filter((doc) => doc.data().difficulty === "Medium");

  // for all documents
  const userPromises = easyDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Pronouns")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userPronounsListSize = doc.data().userPronounsList.length;
    var machinePronounsListSize = machineDoc.data().pronounsList.length;

    var pronounsPercentage = userPronounsListSize / machinePronounsListSize * 100;
    pronounsMediumPercentageList.push(pronounsPercentage);
  });
  await Promise.all(userPromises);

  var pronounsMediumAverage = Math.floor(pronounsMediumPercentageList.reduce((a, b) => a + b, 0) / pronounsMediumPercentageList.length);
  pronounsMediumPercentageList.length = 0;

  return pronounsMediumAverage;
});

exports.getProgressByDifficulty = functions.https.onCall(async (data, context) => {
  // getProgressByDifficulty({userId: "aXFXQnvu7oe3xG90WViYlGO0oGE2", difficulty: "Easy"});
  // Validate input
  if (!data.userId) {
    throw new functions.https.HttpsError('invalid-argument', 'The function must be called with a valid userId.');
  }
  if (!data.difficulty || !['Easy', 'Medium', 'Hard'].includes(data.difficulty)) {
    throw new functions.https.HttpsError('invalid-argument', 'The function must be called with a valid difficulty level (Easy, Medium, or Hard).');
  }

  try {
    // Function to fetch and filter quizzes based on difficulty
    const fetchAndFilterQuizzes = async (category) => {
      const attemptedPath = `users/${data.userId}/problems_attempted/AdminAssessment/${category}`;
      const generatedPath = `problems/Categories/${category}`;

      const attemptedQuizzes = await admin.firestore().collection(attemptedPath).get();
      const generatedQuizzes = await admin.firestore().collection(generatedPath).get();

      return {
        attempted: attemptedQuizzes.docs.filter(doc => doc.data().difficulty === data.difficulty),
        total: generatedQuizzes.docs.filter(doc => doc.data().difficulty === data.difficulty)
      };
    };

    // Fetch and filter quizzes for each category
    const categories = ['Adjectives', 'Nouns', 'Pronouns', 'Verbs'];
    const quizzes = await Promise.all(categories.map(fetchAndFilterQuizzes));

    // Calculate progress
    const attemptedCount = quizzes.reduce((acc, quiz) => acc + quiz.attempted.length, 0);
    const totalCount = quizzes.reduce((acc, quiz) => acc + quiz.total.length, 0);

    return totalCount === 0 ? 0 : (attemptedCount / totalCount) * 100;
  } catch (error) {
    console.error('Error fetching quizzes:', error);
    throw new functions.https.HttpsError('internal', 'Unable to calculate progress.');
  }
});

exports.getPronounsHard = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_attempted")
    .doc("AdminAssessment")
    .collection("Pronouns")
    .get();

  // Get the documents in the "Adjectives" collection
  const documents = userRef.docs;

  // Filter documents where "difficulty" is equal to "Easy"
  const easyDocuments = documents.filter((doc) => doc.data().difficulty === "Hard");

  // for all documents
  const userPromises = easyDocuments.map(async (doc) => {
    // Get the machine's version of the problem
    const machineDoc = await admin.firestore()
      .collection("problems")
      .doc("Categories")
      .collection("Pronouns")
      .doc(doc.id)
      .get();

    // Calculate the percentage of each type of word used
    var userPronounsListSize = doc.data().userPronounsList.length;
    var machinePronounsListSize = machineDoc.data().pronounsList.length;

    var pronounsPercentage = userPronounsListSize / machinePronounsListSize * 100;
    pronounsHardPercentageList.push(pronounsPercentage);
  });
  await Promise.all(userPromises);

  var pronounsHardAverage = Math.floor(pronounsHardPercentageList.reduce((a, b) => a + b, 0) / pronounsHardPercentageList.length);
  pronounsHardPercentageList.length = 0;

  return pronounsHardAverage;
});

// SELF ASSESSMENT

exports.getSelfAssessmentAdjectives = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_generated")
    .get();
  // for all documents
  const userPromises = userRef.docs.map(async (doc) => {

    // Calculate the percentage of each type of word used    
    var userAdjectivesListSize = doc.data().userAdjectivesList.length;

    var apiAdjectivesListSize = doc.data().apiAdjectivesList.length;

    var adjectivesPercentage = userAdjectivesListSize / apiAdjectivesListSize * 100;

    // Add the percentages to the respective lists
    adjectivesSelfAssessmentPercentageList.push(adjectivesPercentage);

  });
  await Promise.all(userPromises);

  var adjectivesSelfAssessmentAverage = Math.floor(adjectivesSelfAssessmentPercentageList.reduce((a, b) => a + b, 0) / adjectivesSelfAssessmentPercentageList.length);

  return adjectivesSelfAssessmentAverage;
});

exports.getSelfAssessmentVerbs = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_generated")
    .get();
  // for all documents
  const userPromises = userRef.docs.map(async (doc) => {

    // Calculate the percentage of each type of word used    
    var userVerbsListSize = doc.data().userVerbsList.length;

    var apiVerbsListSize = doc.data().apiVerbsList.length;

    var verbsPercentage = userVerbsListSize / apiVerbsListSize * 100;

    // Add the percentages to the respective lists
    verbsSelfAssessmentPercentageList.push(verbsPercentage);

  });
  await Promise.all(userPromises);

  var verbsSelfAssessmentAverage = Math.floor(verbsSelfAssessmentPercentageList.reduce((a, b) => a + b, 0) / verbsSelfAssessmentPercentageList.length);

  return verbsSelfAssessmentAverage;
});

exports.getSelfAssessmentNouns = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_generated")
    .get();
  // for all documents
  const userPromises = userRef.docs.map(async (doc) => {

    // Calculate the percentage of each type of word used    
    var userNounsListSize = doc.data().userNounsList.length;

    var apiNounsListSize = doc.data().apiNounsList.length;

    var nounsPercentage = userNounsListSize / apiNounsListSize * 100;

    // Add the percentages to the respective lists
    nounsSelfAssessmentPercentageList.push(nounsPercentage);

  });
  await Promise.all(userPromises);

  var nounsSelfAssessmentAverage = Math.floor(nounsSelfAssessmentPercentageList.reduce((a, b) => a + b, 0) / nounsSelfAssessmentPercentageList.length);

  return nounsSelfAssessmentAverage;
});

exports.getSelfAssessmentPronouns = functions.https.onCall(async (data, context) => {
  const userRef = await admin.firestore()
    .collection("users")
    .doc(data.userId)
    .collection("problems_generated")
    .get();
  // for all documents
  const userPromises = userRef.docs.map(async (doc) => {

    // Calculate the percentage of each type of word used    
    var userPronounsListSize = doc.data().userPronounsList.length;

    var apiPronounsListSize = doc.data().apiPronounsList.length;

    var pronounsPercentage = userPronounsListSize / apiPronounsListSize * 100;

    // Add the percentages to the respective lists
    pronounsSelfAssessmentPercentageList.push(pronounsPercentage);

  });
  await Promise.all(userPromises);

  var pronounsSelfAssessmentAverage = (pronounsSelfAssessmentPercentageList.reduce((a, b) => a + b, 0) / pronounsSelfAssessmentPercentageList.length);

  return pronounsSelfAssessmentAverage;
});



// If visiting after a while, run command "firebase login --reauth" to gain access to project again
// run command "firebase functions:shell" to run the functions within CMD
// getName({userId: "VYZsfT6E9yZ7uNHrICKPi4CbJgm2"});
// getAverage({userId: "VYZsfT6E9yZ7uNHrICKPi4CbJgm2"});
// apiRequest({myString: "Deep Ate an Apple"});
// getAdjectivesEasy({userId: "aXFXQnvu7oe3xG90WViYlGO0oGE2"});
// getAdjectivesMedium({userId: "aXFXQnvu7oe3xG90WViYlGO0oGE2"});
// getAdjectivesHard({userId: "aXFXQnvu7oe3xG90WViYlGO0oGE2"});

