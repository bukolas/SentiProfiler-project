There is a cool view of the whole non-parametric Bayes thing that I think is very instructive. It's easiest to see in the case of the Pitman-Yor language modeling work by Frank Wood and Yee Whye Teh. The view is "memorize what you can, and back off (to a parametric model) when you can't." This is basically the "backoff" view... where NP Bayes comes in is to control the whole "what you can" aspect. In other words, if you're doing language modeling, try to memorize two grams; but if you haven't seen enough to be confident in your memorization, back off to one grams; and if you're not confident there, back off to a uniform distribution (which is our parametric model -- the base distribution). Or, if you think about the state-splitting PCFG work (done both at Berkeley and at Stanford), basically what's going on is that we're memorizing as many differences of tree labels as we can, and then backing off to the "generic" label when memorization fails. Or if we look at Trevor Cohn's NP-Bayes answer to DOP, we see a similar thing: memorize big tree chunks, but if you can't, fall back to a simple CFG (our parametric model). Now, the weird thing is that this mode of memorization is kind of backwards from how I (as an outsider) typically interpret cognitive models (any cogsci people out there, feel free to correct me!). If you take, for instance, morphology, there's evidence that this is exactly not what humans do. We (at least from a popular science) perspective, basically memorize simple rules and then remember exceptions. That is, we remember that to make the past tense of a verb, we add "-ed" (the sound, not the characters) but for certain verbs, we don't: go/went, do/did, etc. You do little studies where you ask people to inflect fake words and they generally follow the rule, not the exceptions (but see * below).