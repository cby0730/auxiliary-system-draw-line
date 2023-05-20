from gtts import gTTS
from playsound import playsound
def say(text):
    audio = gTTS( text = text, lang = 'en' )
    name = '{}.mp3'.format(text)
    try:
        playsound(name)
    except:
        audio.save(name)
        playsound(name)

speechClasses = [
    "person",
    "bicycle",
    "car",
    "motorcycle",
    "airplane",
    "bus",
    "train",
    "truck",
    "traffic light",
    "bench",
    "cat",
    "dog",
    "turn left",
    "turn right"
]

if __name__ == "__main__" :
    for a in speechClasses :
        say(a)