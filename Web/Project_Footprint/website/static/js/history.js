window.myFunction = function(id) {
                    if(document.getElementById("title" + id) != null){
                        var title = document.getElementById("title" + id).textContent;
                    }
                    if(mood = document.getElementById("mood" + id) != null){
                        var mood = document.getElementById("mood" + id).textContent;
                    }
                    if(document.getElementById("image" + id) != null){
                        var image = document.getElementById("image" + id).src;
                        document.getElementById("img").src = image;
                    }else{
                        document.getElementById("img").src="../static/logo3.png";
                    }
                    if(document.getElementById("comment" + id) != null){
                        var comment = document.getElementById("comment" + id).textContent;
                        comment = comment.replace(/(?:\r\n|\r|\n)/g, '<br />');
                    }
                    if (document.getElementById("place" + id) != null){
                        var place = document.getElementById("place" + id).textContent;
                    }
                    if (document.getElementById("custom-place" + id) != null){
                        var customplace = document.getElementById("custom-place" + id).textContent;
                    }
                    if(document.getElementById("created-at" + id) != null){
                        var create = document.getElementById("created-at" + id).textContent;
                    }
                    if(title != null){
                        document.getElementById("demo-title").innerHTML = title;
                    }else{
                        document.getElementById("demo-title").innerHTML = " ";
                    }
                    if(mood != null){
                        document.getElementById("demo-mood").innerHTML = mood;
                    }else{
                        document.getElementById("demo-mood").innerHTML = "";
                    }
                    if(comment != null){
                        document.getElementById("demo-comment").innerHTML = comment
                    }else{
                        document.getElementById("demo-comment").innerHTML = "";
                    }
                    if(place != null){
                        document.getElementById("demo-place").innerHTML = place;
                    }else{
                        document.getElementById("demo-place").innerHTML = "";
                    }
                    if(customplace != null){
                        document.getElementById("demo-custom-place").innerHTML = customplace;
                    }else{
                        document.getElementById("demo-custom-place").innerHTML = "";
                    }
                    if(create != null){
                        document.getElementById("demo-create-at").innerHTML = create;
                    }else{
                        document.getElementById("demo-create-at").innerHTML = "";
                    }
                }