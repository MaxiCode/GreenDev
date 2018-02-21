import numpy as np
import tensorflow as tf

NUM_DIGITS = 10
NUM_HIDDEN = 100
BATCH_SIZE = 128

def binary_encode(i, num_digits):
    return np.array([i >> d & 1 for d in range(num_digits)])

def fizz_buzz_encode(i):
    if   i % 15 == 0: return np.array([0,0,0,1])
    elif i % 5  == 0: return np.array([0,0,1,0])
    elif i % 3  == 0: return np.array([0,1,0,0])
    else:             return np.array([1,0,0,0])

def init_weights(shape):
    return tf.Variable(tf.random_normal(shape, stddev=0.01))

def model(X, w_h, w_o):
    h = tf.nn.relu(tf.matmul(X, w_h))
    return tf.matmul(h, w_o)

def fizz_buzz(i, prediction):
    return [str(i), "fizz", "buzz", "fizzbuzz"][prediction]

def main():
    # training data
    trX = np.array([binary_encode(i, NUM_DIGITS) for i in range(101, 2 ** NUM_DIGITS)])
    trY = np.array([fizz_buzz_encode(i)          for i in range(101, 2 ** NUM_DIGITS)])

    # input and output layer
    X = tf.placeholder("float", [None, NUM_DIGITS])
    Y = tf.placeholder("float", [None, 4])

    # weights to hidden layer
    w_h = init_weights([NUM_DIGITS, NUM_HIDDEN])
    # weights to output layer
    w_o = init_weights([NUM_HIDDEN, 4])

    # model
    py_x = model(X, w_h, w_o)

    cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=py_x, labels=Y))
    train_op = tf.train.GradientDescentOptimizer(0.05).minimize(cost)

    predict_op = tf.argmax(py_x, 1)

    with tf.Session() as sess:
        tf.global_variables_initializer().run()

        for epoch in range(10000):
            p = np.random.permutation(range(len(trX)))
            trX, trY = trX[p], trY[p]

            for start in range(0, len(trX), BATCH_SIZE):
                end = start + BATCH_SIZE
                sess.run(train_op, feed_dict={X: trX[start:end], Y: trY[start:end]})
            if epoch % 1000 == 0:
                print(epoch, np.mean(np.argmax(trY, axis=1) == 
                    sess.run(predict_op, feed_dict={X: trX, Y: trY})))

        numbers = np.arange(1, 101)
        teX = np.transpose(binary_encode(numbers, NUM_DIGITS))

        teY = sess.run(predict_op, feed_dict={X: teX})
        output = np.vectorize(fizz_buzz)(numbers, teY)

        print(output)


if __name__ == "__main__":
    main()