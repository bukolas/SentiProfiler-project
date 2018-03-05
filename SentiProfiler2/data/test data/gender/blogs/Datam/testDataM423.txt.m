 I really enjoyed Mark Dredze's talk at EMNLP on multiclass confidence weighted algorithms, where they take their CW binary predictors and extend them in two (basically equivalent) ways to a multiclass/structured setting (warning: I haven't read the paper!). Mark did a great job presenting, as always, and dryly suggested that we should all throw away our perceptrons and our MIRAs and SVMs and just switch to CW permanently. It was a pretty compelling case. Now, I'm going to pick on basically every "yet another classifier" paper I've read in the past ten years (read: ever). I'm not trying to point fingers, but just try to better understand why I, personally, haven't yet switched to using these things and continue to use either logistic regression or averaged perceptron for all of my classification needs (aside from the fact that I am rather fond of a particular software package for doing these things -- note, though, that it does support PA and maybe soon CW if I decide to spend 10 minutes implementing it!). Here's the deal. Let's look at SVM versus logreg. Whether this is actually true or not, I have this gut feeling that logreg is much less sensitive to hyperparameter selection than are SVMs. This is not at all based on any science, and the experience that it's based on it somewhat unfair (comparing megam to libSVM, for instance, which use very different optimization methods, and libSVM doesn't do early stopping while megam does). However, I've heard from at least two other people that they have the same internal feelings. In other words, here's a caricature of how I believe logreg and SVM behave: