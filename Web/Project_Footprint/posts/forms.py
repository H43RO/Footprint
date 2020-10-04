from django.forms

class PostForm(forms.ModelForm):
    class Meta:
        model = Post
        fields = ['title', 'description']