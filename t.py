import math


def fact(n):
    if n == 0 or n == 1:
        return 1
    else:
        return fact(n-1)*n


def is_prime(n):
    if n < 2:
        return False
    for i in range(2, int(math.sqrt(n))+1):
        if n % i == 0:
            return False
    return True


def SumFactorialsOfPrimes(primeX: int):
    pr_sum = 0
    for i in range(2, primeX+1):
        if is_prime(i):
            pr_sum += fact(i)
    return pr_sum


def Vending(orderSum: int, clientSum: int):
    mas = [5000, 2000, 1000, 500, 200, 100, 50, 10, 5, 2, 1]
    diff = clientSum - orderSum
    dictt = {}
    for m in mas:
        if diff >= m:
            dictt[m] = 0
            while diff >= m:
                dictt[m] += 1
                diff -= m
    return dictt


def Linses(dioptries: list):
    pair = 0
    len_count = {}
    for d in dioptries:
        len_count[d] = len_count.get(d, 0) + 1
    remain_len = []
    for d, count in len_count.items():
        p = count // 2
        pair += p
        rem = count % 2
        if rem > 0:
            remain_len.append(d)
    remain_len.sort()
    for l in remain_len:
        if l - 1 in remain_len:
            remain_len.remove(l - 1)
        elif l + 1 in remain_len:
            remain_len.remove(l + 1)
        else:
            pair += 1
    return pair
    
print(SumFactorialsOfPrimes(5))
print(Vending(21, 50))
print(Linses([-1, 0, 1]))
