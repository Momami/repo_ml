from collections import Counter
import time
def read_dictionary(filename="russian_dictionary.txt"):
    with open(filename, 'r') as file:
        list_words = file.read().split('\n').lower()
    return list_words

def k_gramms():
    list_words = read_dictionary()
    word_dict = dict()
    for index, word in enumerate(list_words):
        word = '$' + word + '$'
        i, ln = 0, len(word) - 2
        while i < ln:
            gramm = word[i:i + 3]
            word_dict.setdefault(gramm, list()).append(index)
            i += 1
    return word_dict, list_words

def levenstein(s1, s2):
    n, m = len(s1) + 1, len(s2) + 1
    if n < m:
        n, m = m, n
        s1, s2 = s2, s1
    mas = [i for i in range (m)]
    for i in range (1, n):
        mas_pre = mas
        mas = [i] + [0] * (m - 1)
        mas[0] = i
        for j in range (1, m):
            temp = mas_pre[j - 1]
            if s2[j - 1] != s1[i - 1]:
                temp += 1
            mas[j] = min(temp, mas_pre[j] + 1, mas[j - 1] + 1)
    return mas[m - 1]

def process_word(search_word, dictionary, lst_w):
    start_time = time.time()
    search_word = '$' + search_word + '$'
    lst = list()
    i, ln = 0, len(search_word) - 2
    while i < ln:
        gramm = search_word[i:i + 3]
        lst += dictionary.get(gramm, [])
        i += 1
    count_words = Counter(lst)
    best_words = \
        sorted(count_words, reverse=True, key=count_words.get)[:min(len(count_words), 2000)]
    list_lev = dict()
    for word in best_words:
        list_lev[word] = levenstein(lst_w[word], search_word)
    print("--- %s seconds ---" % (time.time() - start_time))
    for i, index in enumerate(sorted(list_lev, key=list_lev.get)):
        if i == 10:
            break
        print(lst_w[index])


if __name__ == "__main__":
    d, lst_w = k_gramms()
    while True:
        f = input("Выберите действие:\n1-Найти слово\n2-Выйти\n")
        if f == '1':
            word = input("Введите слово для поиска: ")
            process_word(word.lower(), d, lst_w)
        elif f == '2':
            break