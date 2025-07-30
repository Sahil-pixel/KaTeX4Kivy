from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.utils import platform
from kivy.clock import Clock

if platform == 'android':
    from katex4kivy import KatexImage


class ExampleKaTeX(BoxLayout):
    def __init__(self, **kwargs):
        super().__init__(orientation='vertical', **kwargs)
        if platform == 'android':
            Clock.schedule_once(self.add_katex_widget, 0.2)  # 200ms delay

    def add_katex_widget(self, dt):

        tex1 = r'''
        \begin{aligned}
            E &= mc^2 \quad & \text{(Einstein)} \\
            a^2 + b^2 &= c^2 \quad & \text{(Pythagorean theorem)} \\
            \sum_{n=1}^{\infty} \frac{1}{n^2} &= \frac{\pi^2}{6} \\
            \int_0^1 x^2 \, dx &= \frac{1}{3} \\
            \lim_{x \to 0} \frac{\sin x}{x} &= 1 \\
            \binom{n}{k} &= \frac{n!}{k!(n-k)!} \\
            \vec{F} &= m\vec{a} \\
            \int_0^1 x^2 \, dx &= \frac{1}{3} \\
            \end{aligned} 
              '''
        katex1 = KatexImage(
            latex=tex1,
            font_size="24px",
            text_color="#000000",
            background_color="#FFFFFF"
        )

        self.add_widget(katex1)
        tex2 = "\\int_0^\\infty e^{-x^2} dx = \\frac{\\sqrt{\\pi}}{2}"
        katex2 = KatexImage(
            latex=tex2,
            font_size="24px",
            text_color="#0000FF",
            background_color="#00FF00"
        )
        self.add_widget(katex2)


class HelloKaTeXApp(App):
    def build(self):
        return ExampleKaTeX()


if __name__ == "__main__":
    HelloKaTeXApp().run()
