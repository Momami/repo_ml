from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_distances
import sklearn.metrics as mt
import pandas as pd
import json
import numpy as np
import math
import pymorphy2
from nltk.stem.snowball import RussianStemmer
from nltk.corpus import stopwords
import nltk
import string
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression


nltk.download('stopwords')
cachedStopWords = stopwords.words("russian")
nltk.download('punkt')
porter_stemmer = RussianStemmer()
morph = pymorphy2.MorphAnalyzer()
punct = string.punctuation


def preparation_docs(docs):
    for i, field in enumerate(docs):
        nltk_tokens = nltk.word_tokenize(field)
        nltk_tokens = [morph.parse(word)[0].normal_form for word in nltk_tokens
                       if word not in cachedStopWords and word not in punct]
        nltk_tokens = ' '.join(porter_stemmer.stem(w_port) for w_port in nltk_tokens)
        docs[i] = nltk_tokens
    return docs


def vectorizer(data):
    docs = list()
    for elem in data['answer']:
        docs.extend(elem.values())
    docs = preparation_docs(docs)
    query = preparation_docs([data["query"]])
    tfidf_vectorizer = TfidfVectorizer(use_idf=True, min_df=1, token_pattern='(?u)\\b\\w+\\b')
    fitten = tfidf_vectorizer.fit(docs)
    tfidf_vectorizer_vectors = tfidf_vectorizer.transform(docs)
    query_vec = fitten.transform(query)
    cosine = np.array([1 - cosine_distances(vec, query_vec)[0] for vec in tfidf_vectorizer_vectors])
    i = 2
    new_cos = list()
    while i < len(cosine):
        new_cos.append([cosine[i - 2][0], cosine[i - 1][0], cosine[i][0]])
        i += 3
    return new_cos


def read_file(files, nDCG):
    mat = []
    for file in files:
        with open(file, "r", encoding="utf-8") as read_file:
            data = json.load(read_file)
        cosine = list(vectorizer(data))
        sc_title = 0.5
        sc_author = 0.4
        sc_publisher = 0.1
        for i, elem in enumerate(cosine):
            cosine[i].extend([sc_author * elem[0] + sc_publisher * elem[1] + sc_title * elem[2], i])
        sc, i, x = 5, 0, 1
        len_mat = len(cosine)
        gr = len_mat / 5
        cosine_sort = sorted(cosine, key=lambda a_entry: a_entry[3], reverse=True)
        while i < len_mat:
            cosine_sort[i][3] = sc
            if i > gr:
                sc -= 1
                x += 1
                gr = len_mat * x / 5
            i += 1
        # ndcg
        nDCG.append(ndcg_score(cosine_sort))
        mat.extend(cosine[0:len(cosine)][:])
    return mat


def ndcg_score(cosine_sort):
    idcg = 0
    for i, el in enumerate(cosine_sort):
        idcg += el[3] / math.log(i + 2)
    cosine = sorted(cosine_sort, key=lambda a_entry: a_entry[4])
    dcg = 0
    for el in cosine:
        dcg += el[3] / math.log(el[4] + 2)
    ndcg = dcg / idcg
    return ndcg


def dcg_score(mas):
    dcg = 0
    for i, el in enumerate(mas):
        dcg += el / math.log(i + 2)
    return dcg


def score_query(nDCG):
    files = list()
    for i in range(11, 21):
        files.append("query" + str(i) + ".json")
    mat = read_file(files, nDCG)
    score = list()
    for i, elem in enumerate(mat):
        mat[i] = elem[:3]
        score.append(elem[3])
    #print(mat)
    return mat, score


if __name__ == "__main__":
    nDCG = list()
    mat, score = score_query(nDCG)
    print(nDCG)
    nDCG = sum(nDCG)/len(nDCG)
    print(nDCG)
    # nDCG и его улучшение с помощью ML
    X_train, X_test, y_train, y_test = train_test_split(mat, score, test_size=0.3)
    regressor = LinearRegression()
    # Тренеруем модель
    regressor.fit(X_train, y_train)
    y_pred = regressor.predict(X_test)
    print(y_pred)

    dcg = dcg_score(sorted(y_pred, reverse=True))
    idcg = dcg_score(sorted([y_test, y_pred], key=lambda a_entry: a_entry[0], reverse=True)[1])
    print(dcg/idcg)