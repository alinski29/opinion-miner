# Oppinion miner
Mine pairs of (aspect, sentiment), with aspect topics and sentiment scores from customer reviews.

<details open>
  <summary><b>Table of contents</b></summary>

---
- [Context](#context)
- [Usage](#usage)
- [Build](#build)
- [Tweaks](#tweaks)
  - [Changing word vectors](#changing-word-vectors)
  - [Changing sentiment scores](changing-sentiment-scores)
  - [Changing topics](changing-topics)
---

</details>

## **Context**
Traditional opinion mining extracts pairs of (aspect, sentiment) fron text by using NLP models for lemmatization, part-of-speech tagging, dependency parsing.
This work builds upon that by combining this approach with word vectors ([word2vec](https://en.wikipedia.org/wiki/Word2vec)), which are used to classify the aspects into user-defined topics by determining how similar their vectors are. Sentiment scores are assigned to sentiment words, making it possible to create topic-level aggregations.

---
## **Usage**
```scala
import com.github.alinski.opinion.miner.OpinionMiner._

val review = """
    |  The food was fantastic, but the service was really slow, although the waiter was very knowledgeable.
    |  The pumpkin soup was delightful, perfect for the fall season.
    |  Impressive rooftop and a terrific view. Prices were reasonable.
  """.stripMargin

val opinions = OpinionMiner(review)
opinions.foreach(println)

(Aspect(waiter ∈ [service]), Sentiment(knowledgeable, 4.38))
(Aspect(service ∈ [service]), Sentiment(slow, 2.68))
(Aspect(food ∈ [food]), Sentiment(fantastic, 4.61))
(Aspect(pumpkin soup ∈ [food]), Sentiment(delightful, 4.45))
(Aspect(view ∈ [ambiance]), Sentiment(Impressive, 4.19))
(Aspect(view ∈ [ambiance]), Sentiment(terrific, 4.54))
(Aspect(rooftop ∈ [location]), Sentiment(Impressive, 4.19))

```
See [example](https://github.com/alinski29/opinion-miner/blob/master/src/main/scala/com/github/alinski/opinion/miner/ExampleApp.scala)

---

## **Build**
Currently you have to build it from source using sbt. Should be soon available on a public repository.
```bash
sbt clean assembly
```
---

## **Tweaks**

### **Changing word vectors**
The word vectors are trained based on the [Yelp Dataset Challenge](https://www.yelp.com/dataset) 2016, on ~4 milion reviews using [FastText](https://fasttext.cc/) skipgram model. To keep the file size reasonable, only word lemmas wore kept and some infrequent words were removed.
If you want better precision or a different context, you may input your own word vectors, by providing a file with the same structure as `src/main/resources/words.vec`.

### **Changing sentiment scores**
Sentiment scores were computed for words a ngrams from ~4 mil Yelp reviews. The values are in `src/main/resources/sentimentScores.txt`. Change this file if you have different scores.

### **Changing topics**
Any number of topics can be created by modifying `src/main/resources/topicsMain.txt` / `topicsSecondary.txt`. 
Topics are defined in a the following format: 1 topic per line, comma separated, first word is the topic name, the following are example members (seed words). The seed words are used to attribute a word to a topic base on their cosine similarity.

---

