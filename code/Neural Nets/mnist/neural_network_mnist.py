from __future__ import print_function

# Import MNIST data

import input_data
mnist = input_data.read_data_sets("./data/", one_hot=True)

import tensorflow as tf

# Parameter
learning_rate = 0.01
num_steps = 50000
batch_size = 128
display_step = 1000

# Network Parameters
n_hidden_1  = 256 # 1st hidden layer
n_hidden_2  = 256 # 2nd hidden layer
num_input   = 784 # input layer
num_classes = 10  # output layer
dropout = 0.25

# tf Graph input
X = tf.placeholder("float", [None, num_input])
Y = tf.placeholder("float", [None, num_classes])

# Store layer weights and biases
weights = {
	'h1' : tf.Variable(tf.random_normal([num_input, n_hidden_1])),
	'h2' : tf.Variable(tf.random_normal([n_hidden_1, n_hidden_2])),
	'out': tf.Variable(tf.random_normal([n_hidden_2, num_classes]))
}
biases = {
	'b1' : tf.Variable(tf.random_normal([n_hidden_1])),
	'b2' : tf.Variable(tf.random_normal([n_hidden_2])),
	'out': tf.Variable(tf.random_normal([num_classes]))
}

# Create Model
def neural_net(x):
	# Hidden fully connected layer with 256 neurons
	layer_1 = tf.add(tf.matmul(x, weights['h1']), biases['b1'])
	# Hidden fully connected layer with 256 neurons
	layer_2 = tf.add(tf.matmul(layer_1, weights['h2']), biases['b2'])
	# Output fully connected layer with a neuron for each class
	out_layer = tf.add(tf.matmul(layer_2, weights['out']), biases['out'])
	return out_layer

# Construct model
logits = neural_net(X)
prediction = tf.nn.softmax(logits)

# Define loss or optimizer
loss_op = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(
	logits=logits, labels=Y))
optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate)
train_op = optimizer.minimize(loss_op)

# Evaluate model
correct_pred = tf.equal(tf.argmax(prediction, 1), tf.argmax(Y, 1))
accuracy = tf.reduce_mean(tf.cast(correct_pred, tf.float32))

# Start training
with tf.Session() as sess:

	# Run the initializer
	tf.global_variables_initializer().run()

	for step in range(1, num_steps+1):
		batch_x, batch_y = mnist.train.next_batch(batch_size)
		
		# Run optimizer op (backprop)
		sess.run(train_op, feed_dict={X: batch_x, Y: batch_y})

		# print progress 
		if step % display_step == 0 or step == 1:
			#Calculate batch loss and accuracy
			loss, acc = sess.run([loss_op, accuracy], 
				feed_dict={X: batch_x, Y: batch_y})

			print("Step: " + str(step) + ", Minibatch Loss= " + \
				"{:.4f}".format(loss) + ", Training Accuracy= " + \
				"{:.3f}".format(acc))

	print("Optimization Finished.")

	# Calculate accuracy for MNIST test images
	print("Testing Accuracy: ",\
		sess.run(accuracy, 
			feed_dict={X: mnist.test.images, Y: mnist.test.labels}))
