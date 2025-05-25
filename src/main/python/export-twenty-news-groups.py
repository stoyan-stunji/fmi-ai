from sklearn.datasets import fetch_20newsgroups
import os

newsgroups = fetch_20newsgroups(subset='all', remove=('headers', 'footers', 'quotes'))

output_dir = '20newsgroups'
os.makedirs(output_dir, exist_ok=True)

for i, text in enumerate(newsgroups.data):
    category = newsgroups.target_names[newsgroups.target[i]]
    category_dir = os.path.join(output_dir, category.replace('.', '_'))
    os.makedirs(category_dir, exist_ok=True)

    filename = os.path.join(category_dir, f'doc_{i}.txt')
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(text)
