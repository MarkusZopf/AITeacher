# AITeacher

## Motivation
Traditionally, students are educated by human teachers in schools where a one-size-fits-all approach is applied. In practice, it turns out that it is rather a one-size-fits-nobody situation since students learn different topics at different paces, need different levels of acknowledgements of their progress, and want to learn at different times and in different places. Every student is an individual and in an ideal scenario we would like to have one teacher per student who is able to adapt to the individual needs, motivates the students to carry on learning, and is available everywhere 24/7. No human teacher will be able to provide this level of personalization.
In this project, we present an adaptive motivating online learning platform, AITeacher, which complements human teachers by addressing the presented issues in traditional education.

## Approach
AITeacher asks students questions which are selected to fit the individual needs of the student. This motivates them to carry on learning since no student is neither overburdened nor insufficiently challenged. We measure individuality according to two dimensions, individual skill and individual need for acknowledgement. AITeacher uses machine learning to estimate both values for each user individually.
We first adopt the idea of collaborative filtering which is usually applied in recommender systems such as Amazon or Netflix to estimate the individual skill of students. Given an interaction history with the platform (a series of answered questions), we search for other users with similar interaction histories. We then compute the probabilities that a given user will answer correctly for all questions based on his or her nearest neighbors.
Second, we estimate the optimal rate of correctly answered questions (desired level of progress acknowledgement) for each student. If a student is above the optimal rate in his or her current learning session, the platform will select more challenging questions. On the other hand, it will select easier questions if the student falls below the optimal rate. After each session, we update the optimal rate of correctly answered questions estimate to converge to the true optimal rate.
If questions were answered incorrectly in the past, AITeacher provides answer hints in from of automatically retrieved images when asking the questions again. Finally, AITeacher is available 24/7. Students can learn at any time wherever they want.

## Technology & Resources
AITeacher is written as a Java servlet in a Maven project and run by an embedded Jetty server. We use the AI2 Science Questions Mercury dataset to feed the platform. To give answer hints, we invoke the Bing Image Search API. All data such as user information and question histories is stored in a MySQL database.

## Outlook
The prototype can be extended in various ways. For example, we can use automatic paraphrasing of questions and answers to add more variation to the dataset similarly to automatically search for answer hints. The model currently selects the questions which fits best to the student’s needs. However, the model also needs to explore questions to estimate the answer probability better. To find a good exploration/exploitation-trade-off, a multi-armed bandit model could be applied.
