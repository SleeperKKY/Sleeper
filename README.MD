#Sleeper


*There are many sleep tracking apps in android market, and wearable gadgets on the market. They track your sleep stage during
your sleep and gives analysis of how you slept well. But that's all. They don't offer you some treatment to improve your
sleep quality. Some users might want app to improve their sleep quality too. So we thought about how to improve users sleep
quality using sleep stage. Then we got to the idea, in which we build an app that not only tracks sleep stage but also control
remote device, like in summer fan, air conditional, or in winter heater, via wifi. We also thought that it should be opensource so
developer can make and modify our app much better than now since this is our first android app ever made. So we'd like to get
advice to design of classes, other feature to add or even harsh feedback!!.*


##Application

*We separated library package, 'propclasses', and user implementation package, currently 'endclasses'. Propclasses consist
of 'app_manager' package, 'communition_manager', 'dataprocessor_manager'.* 

###app_manager
*App_manager package is head part that does control trivial things other than tracking sleep tracking like broadcasting alarm intent, get wakelock sort of thing.*

###dataprocessor_manager 
*Dataprocessor_manager package is where actual tracking of sleep is taking place. It uses libsvm library made by 
cnbuff410[https://github.com/cnbuff410/Libsvm-androidjni] to train data to classify each sleep stage. Features we are currently
using are only average and pim of intensity values, which is sqrt(x^2+y^2+z^2) of TYPE_LINEAR_ACCELEROMETER of sensor.
We gather intensity values every 50ms, and calculate average and pim every 1 minute. If there are other feature that you
want to add then you should learn how to train using training_set file and make model file. You should go to libsvm link
above to learn.*
